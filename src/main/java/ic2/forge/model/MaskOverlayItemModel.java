package ic2.forge.model;

import com.mojang.blaze3d.platform.NativeImage;
import ic2.core.item.tool.ItemObscurator;
import ic2.core.util.StackUtil;

import java.io.IOException;
import java.util.*;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.SpriteContents;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MaskOverlayItemModel implements Ic2Model, BakedModel
{
	private final ResourceLocation baseModelLocation;
	private final ResourceLocation maskTextureLocation;
	private final boolean scaleOverlay;
	private final float offset;

	private BakedModel baseModel;
	private List<BakedQuad> baseQuads;
	private List<OverlayQuad> overlayTemplates;
	private float uS, vS, uE, vE;

	private final ItemOverrides itemOverrides = new ItemOverrides()
	{
		@Override
		public BakedModel resolve(@NotNull BakedModel originalModel, @NotNull ItemStack stack, @Nullable net.minecraft.client.multiplayer.ClientLevel level, @Nullable LivingEntity entity, int seed)
		{
			CompoundTag nbt = StackUtil.getOrCreateNbtData(stack);
			BlockState state = ItemObscurator.getState(nbt);
			Direction side = ItemObscurator.getSide(nbt);
			int[] colorMultipliers = ItemObscurator.getColorMultipliers(nbt);

			if (state != null && side != null && colorMultipliers != null)
			{
				ItemObscurator.ObscuredRenderInfo renderInfo = ItemObscurator.getRenderInfo(state, side);
				if (renderInfo != null && colorMultipliers.length * 4 == renderInfo.uvs.length)
				{
					List<BakedQuad> overlayQuads = buildOverlayQuads(renderInfo, colorMultipliers);
					return new OverlaidModel(baseModel, baseQuads, overlayQuads);
				}
			}

			return baseModel;
		}
	};

	public MaskOverlayItemModel(ResourceLocation baseModelLocation, ResourceLocation maskTextureLocation, boolean scaleOverlay, float offset)
	{
		this.baseModelLocation = baseModelLocation;
		this.maskTextureLocation = maskTextureLocation;
		this.scaleOverlay = scaleOverlay;
		this.offset = offset;
	}

	private static ResourceLocation maskToResource(ResourceLocation maskLocation)
	{
		return ResourceLocation.fromNamespaceAndPath(maskLocation.getNamespace(), "textures/" + maskLocation.getPath() + ".png");
	}

	private static BakedQuad remapQuad(BakedQuad template, TextureAtlasSprite sprite, float uS, float vS, float uE, float vE, int colorMul)
	{
		int[] oldData = template.getVertices();
		int[] newData = oldData.clone();
		int stride = oldData.length >>> 2;

		// Update UVs: vertices in template are ordered as triangle strip:
		// v0: (xS, yS) bottom-left  → UV (uS, vS)
		// v1: (xE, yS) bottom-right → UV (uE, vS)
		// v2: (xE, yE) top-right    → UV (uE, vE)
		// v3: (xS, yE) top-left     → UV (uS, vE)
		newData[4] = Float.floatToRawIntBits(uS);
		newData[5] = Float.floatToRawIntBits(vS);
		newData[stride + 4] = Float.floatToRawIntBits(uE);
		newData[stride + 5] = Float.floatToRawIntBits(vS);
		newData[2 * stride + 4] = Float.floatToRawIntBits(uE);
		newData[2 * stride + 5] = Float.floatToRawIntBits(vE);
		newData[3 * stride + 4] = Float.floatToRawIntBits(uS);
		newData[3 * stride + 5] = Float.floatToRawIntBits(vE);

		// Apply color multiplier
		if (colorMul != -1)
		{
			float r = ((colorMul >>> 16) & 0xFF) / 255f;
			float g = ((colorMul >>> 8) & 0xFF) / 255f;
			float b = (colorMul & 0xFF) / 255f;

			for (int v = 0; v < 4; v++)
			{
				int off = v * stride + 3;
				int packedColor = oldData[off];
				int a = (packedColor >>> 24) & 0xFF;
				int red = (int) ((packedColor & 0xFF) * r);
				int green = (int) (((packedColor >>> 8) & 0xFF) * g);
				int blue = (int) (((packedColor >>> 16) & 0xFF) * b);
				newData[off] = (a << 24) | (blue << 16) | (green << 8) | red;
			}
		}

		return new BakedQuad(newData, template.getTintIndex(), template.getDirection(), sprite, template.isShade());
	}

	private static BakedQuad createQuad(float xS, float yS, float xE, float yE, float z, Direction face, TextureAtlasSprite sprite)
	{
		int[] data = new int[32]; // 4 vertices × 8 stride
		int normal = computeNormal(face);
		int color = 0xFFFFFFFF;

		// Vertex 0: bottom-left
		data[0] = Float.floatToRawIntBits(xS);
		data[1] = Float.floatToRawIntBits(yS);
		data[2] = Float.floatToRawIntBits(z);
		data[3] = color;
		data[4] = Float.floatToRawIntBits(0f);
		data[5] = Float.floatToRawIntBits(0f);
		data[6] = LightTexture.FULL_BRIGHT;
		data[7] = normal;

		// Vertex 1: bottom-right
		data[8] = Float.floatToRawIntBits(xE);
		data[9] = Float.floatToRawIntBits(yS);
		data[10] = Float.floatToRawIntBits(z);
		data[11] = color;
		data[12] = Float.floatToRawIntBits(1f);
		data[13] = Float.floatToRawIntBits(0f);
		data[14] = LightTexture.FULL_BRIGHT;
		data[15] = normal;

		// Vertex 2: top-right
		data[16] = Float.floatToRawIntBits(xE);
		data[17] = Float.floatToRawIntBits(yE);
		data[18] = Float.floatToRawIntBits(z);
		data[19] = color;
		data[20] = Float.floatToRawIntBits(1f);
		data[21] = Float.floatToRawIntBits(1f);
		data[22] = LightTexture.FULL_BRIGHT;
		data[23] = normal;

		// Vertex 3: top-left
		data[24] = Float.floatToRawIntBits(xS);
		data[25] = Float.floatToRawIntBits(yE);
		data[26] = Float.floatToRawIntBits(z);
		data[27] = color;
		data[28] = Float.floatToRawIntBits(0f);
		data[29] = Float.floatToRawIntBits(1f);
		data[30] = LightTexture.FULL_BRIGHT;
		data[31] = normal;

		return new BakedQuad(data, -1, face, sprite, false);
	}

	private static int computeNormal(Direction face)
	{
		return face.getStepX() & 0xFF | ((face.getStepY() & 0xFF) << 8) | ((face.getStepZ() & 0xFF) << 16);
	}

	private static BitSet readMask(NativeImage img)
	{
		int w = img.getWidth();
		int h = img.getHeight();
		BitSet ret = new BitSet(w * h);

		for (int y = 0; y < h; y++)
		{
			for (int x = 0; x < w; x++)
			{
				if ((img.getPixelRGBA(x, y) >>> 24) > 128)
				{
					ret.set(y * w + x);
				}
			}
		}

		return ret;
	}

	private static List<Area> searchAreas(BitSet pixels, int width)
	{
		List<Area> ret = new ArrayList<>();
		int idx = 0;

		while ((idx = pixels.nextSetBit(idx)) != -1)
		{
			int y = idx / width;
			int x = idx - y * width;
			int areaWidth = Math.min(width - x, pixels.nextClearBit(idx + 1) - idx);
			int areaHeight = 1;

			for (int nextLineIdx = idx + width; pixels.get(nextLineIdx) && pixels.nextClearBit(nextLineIdx + 1) >= nextLineIdx + areaWidth; nextLineIdx += width)
			{
				pixels.clear(nextLineIdx, nextLineIdx + areaWidth);
				areaHeight++;
			}

			ret.add(new Area(x, y, areaWidth, areaHeight));
			idx += areaWidth;
		}

		return ret;
	}

	@Override
	public BakedModel bake(
		IGeometryBakingContext owner,
		ModelBaker bakery,
		Function<Material, TextureAtlasSprite> spriteGetter,
		ModelState modelTransform,
		ItemOverrides overrides,
		ResourceLocation modelLocation
	)
	{
		this.baseModel = bakery.bake(this.baseModelLocation, modelTransform, spriteGetter);
		if (this.baseModel == null)
		{
			throw new IllegalStateException("missing base model " + this.baseModelLocation);
		}

		RandomSource rand = RandomSource.create(42L);
		this.baseQuads = this.baseModel.getQuads(null, null, rand, ModelData.EMPTY, null);

		TextureAtlasSprite maskSprite = spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, this.maskTextureLocation));

		try (SpriteContents tex = maskSprite.contents())
		{
			ResourceLocation maskResLoc = maskToResource(this.maskTextureLocation);
			Optional<Resource> resOpt = Minecraft.getInstance().getResourceManager().getResource(maskResLoc);
			NativeImage maskImage = resOpt.isPresent() ? NativeImage.read(resOpt.get().open()) : null;
			
			int width = maskImage.getWidth();
			int height = maskImage.getHeight();
			List<Area> areas = searchAreas(readMask(maskImage), width);
			maskImage.close();

			float texWidth = tex.width() / 16f;
			float texHeight = tex.height() / 16f;

			float zF = (7.5f - this.offset) / 16f;
			float zB = (8.5f + this.offset) / 16f;

			this.overlayTemplates = new ArrayList<>(areas.size());

			for (Area area : areas)
			{
				float xS = area.x / (float) width * texWidth;
				float yS = 1f - area.y / (float) height * texHeight;
				float xE = (area.x + area.width) / (float) width * texWidth;
				float yE = 1f - (area.y + area.height) / (float) height * texHeight;

				BakedQuad frontQuad = createQuad(xS, yS, xE, yE, zF, Direction.SOUTH, maskSprite);
				BakedQuad backQuad = createQuad(xS, yS, xE, yE, zB, Direction.NORTH, maskSprite);
				this.overlayTemplates.add(new OverlayQuad(frontQuad, backQuad, xS, yS, xE, yE));
			}

			if (this.scaleOverlay)
			{
				calcOverlayBounds(areas, width, height, texWidth, texHeight);
			} else
			{
				this.uS = this.vS = 0f;
				this.uE = this.vE = 1f;
			}
		} catch (IOException e)
		{
			this.overlayTemplates = Collections.emptyList();
			this.uS = this.vS = 0f;
			this.uE = this.vE = 1f;
		}

		return this;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random, @NotNull ModelData extraData, @Nullable net.minecraft.client.renderer.RenderType renderType)
	{
		return this.baseQuads;
	}

	@Override
	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random)
	{
		return this.baseQuads;
	}

	@Override
	public boolean useAmbientOcclusion()
	{
		return this.baseModel.useAmbientOcclusion();
	}

	@Override
	public boolean isGui3d()
	{
		return this.baseModel.isGui3d();
	}

	@Override
	public boolean usesBlockLight()
	{
		return this.baseModel.usesBlockLight();
	}

	@Override
	public boolean isCustomRenderer()
	{
		return false;
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon()
	{
		return this.baseModel.getParticleIcon();
	}

	@Override
	public @NotNull ItemOverrides getOverrides()
	{
		return this.itemOverrides;
	}

	private List<BakedQuad> buildOverlayQuads(ItemObscurator.ObscuredRenderInfo renderInfo, int[] colorMultipliers)
	{
		List<BakedQuad> result = new ArrayList<>(this.overlayTemplates.size() * 2);

		for (int qi = 0; qi < this.overlayTemplates.size(); qi++)
		{
			OverlayQuad template = this.overlayTemplates.get(qi);

			int ti = qi % renderInfo.tints.length;
			TextureAtlasSprite sprite = renderInfo.sprites[ti];
			int colorMul = colorMultipliers[ti];

			float quS = renderInfo.uvs[qi * 4];
			float qvS = renderInfo.uvs[qi * 4 + 1];
			float quE = renderInfo.uvs[qi * 4 + 2];
			float qvE = renderInfo.uvs[qi * 4 + 3];

			float mappedUS = uS + (quS - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) * (uE - uS);
			float mappedVS = vS + (qvS - sprite.getV0()) / (sprite.getV1() - sprite.getV0()) * (vE - vS);
			float mappedUE = uS + (quE - sprite.getU0()) / (sprite.getU1() - sprite.getU0()) * (uE - uS);
			float mappedVE = vS + (qvE - sprite.getV0()) / (sprite.getV1() - sprite.getV0()) * (vE - vS);

			result.add(remapQuad(template.frontQuad, sprite, mappedUS, mappedVS, mappedUE, mappedVE, colorMul));
			result.add(remapQuad(template.backQuad, sprite, mappedUS, mappedVS, mappedUE, mappedVE, colorMul));
		}

		return result;
	}

	private void calcOverlayBounds(List<Area> areas, int width, int height, float texWidth, float texHeight)
	{
		int minX = Integer.MAX_VALUE;
		int minY = Integer.MAX_VALUE;
		int maxX = Integer.MIN_VALUE;
		int maxY = Integer.MIN_VALUE;

		for (Area area : areas)
		{
			if (area.x < minX) minX = area.x;
			if (area.y < minY) minY = area.y;
			if (area.x + area.width > maxX) maxX = area.x + area.width;
			if (area.y + area.height > maxY) maxY = area.y + area.height;
		}

		this.uS = minX / (float) width * texWidth;
		this.vS = 1f - maxY / (float) height * texHeight;
		this.uE = maxX / (float) width * texWidth;
		this.vE = 1f - minY / (float) height * texHeight;
	}

	private record Area(int x, int y, int width, int height)
		{
		}

	private record OverlayQuad(BakedQuad frontQuad, BakedQuad backQuad, float xS, float yS, float xE, float yE)
		{
		}

	private record OverlaidModel(BakedModel baseModel, List<BakedQuad> baseQuads,
	                             List<BakedQuad> overlayQuads) implements BakedModel
		{

			@Override
			public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random, ModelData extraData, @Nullable RenderType renderType)
			{
				if (side != null) return this.baseQuads;
				List<BakedQuad> combined = new ArrayList<>(this.baseQuads.size() + this.overlayQuads.size());
				combined.addAll(this.baseQuads);
				combined.addAll(this.overlayQuads);
				return combined;
			}
	
			@Override
			public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random)
			{
				return this.getQuads(state, side, random, ModelData.EMPTY, null);
			}
	
			@Override
			public boolean useAmbientOcclusion()
			{
				return this.baseModel.useAmbientOcclusion();
			}
	
			@Override
			public boolean isGui3d()
			{
				return this.baseModel.isGui3d();
			}
	
			@Override
			public boolean usesBlockLight()
			{
				return this.baseModel.usesBlockLight();
			}
	
			@Override
			public boolean isCustomRenderer()
			{
				return false;
			}
	
			@Override
			public @NotNull TextureAtlasSprite getParticleIcon()
			{
				return this.baseModel.getParticleIcon();
			}
	
			@Override
			public @NotNull ItemOverrides getOverrides()
			{
				return ItemOverrides.EMPTY;
			}
		}
}
