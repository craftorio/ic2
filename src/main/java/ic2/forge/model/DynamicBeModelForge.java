package ic2.forge.model;

import ic2.core.block.DynamicBeModel;
import ic2.core.block.comp.Obscuration;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

final class DynamicBeModelForge extends DynamicBeModel<List<List<BakedQuad>>> implements Ic2Model
{
	private static final ModelProperty<List<List<BakedQuad>>> MESH_DATA = new ModelProperty<>();

	DynamicBeModelForge(ResourceLocation id)
	{
		super(id);
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
		return super.bake(bakery, spriteGetter, modelTransform, modelLocation);
	}


	public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData)
	{
		BlockEntity be = null;
		boolean active = this.block.canActive() && (be = world.getBlockEntity(pos)) instanceof Ic2TileEntity && ((Ic2TileEntity) be).getActive();
		List<List<BakedQuad>> mesh = this.getMesh(state, active);

		if (be instanceof Ic2TileEntity te)
		{
			Obscuration component = te.getComponent(Obscuration.class);
			if (component != null)
			{
				Obscuration.ObscurationData[] data = component.getRenderState();
				if (data != null)
				{
					mesh = new ArrayList<>(mesh);

					for (int face = 0; face < 6; face++)
					{
						if (data[face] != null)
						{
							List<BakedQuad> obscured = getObscuredQuads(data[face], Util.ALL_DIRS[face]);
							if (obscured != null)
							{
								mesh.set(face, obscured);
							}
						}
					}
				}
			}
		}

		tileData = tileData.derive().with(MESH_DATA, mesh).build();
		assert tileData.get(MESH_DATA) == mesh;
		return tileData;
	}

	public @NotNull List<BakedQuad> getQuads(BlockState state, Direction side, @NotNull RandomSource rand, ModelData extraData, @Nullable RenderType renderType)
	{
		List<List<BakedQuad>> mesh = extraData.get(MESH_DATA);
		return mesh.get(getIdx(side));
	}

	protected List<List<BakedQuad>> generateMesh(BakedModel baseModel, int rot, boolean rotX)
	{
		List<List<BakedQuad>> mesh = new ArrayList<>(7);

		for (int i = 0; i < 7; i++) mesh.add(null);

		for (int i = 0; i < 7; i++)
		{
			Direction face = i < 6 ? Util.ALL_DIRS[i] : null;
			List<BakedQuad> quads = baseModel.getQuads(null, face, null, null, null);
			int writeIdx = i;
			if (rot != 0)
			{
				if (face != null)
				{
					writeIdx = rotateFace(face, rot, rotX).ordinal();
				}

				if (!quads.isEmpty())
				{
					List<BakedQuad> newQuads = new ArrayList<>(quads.size());

					for (BakedQuad quad : quads)
					{
						newQuads.add(rotateQuad(quad, rot, rotX));
					}

					quads = newQuads;
				}
			}

			mesh.set(writeIdx, quads);
		}

		return mesh;
	}

	private static int getIdx(Direction dir)
	{
		return dir == null ? 6 : dir.ordinal();
	}

	@Override
	public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
	{
		return super.getParticleIcon(modelData);
	}

	@Override
	public TextureAtlasSprite getParticleIcon()
	{
		return this.baseModel.getParticleIcon();
	}

	private static List<BakedQuad> getObscuredQuads(Obscuration.ObscurationData data, Direction targetFace)
	{
		BakedModel refModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(data.state);
		RandomSource rand = RandomSource.create(42L);
		List<BakedQuad> refQuads = refModel.getQuads(data.state, data.side, rand, ModelData.EMPTY, null);

		if (refQuads.isEmpty())
		{
			return null;
		}

		List<BakedQuad> result = new ArrayList<>(refQuads.size());

		for (int i = 0; i < refQuads.size(); i++)
		{
			BakedQuad quad = refQuads.get(i);

			if (data.side != targetFace)
			{
				quad = transformQuadFace(quad, data.side, targetFace);
			}

			if (data.colorMultipliers != null && i < data.colorMultipliers.length && data.colorMultipliers[i] != -1)
			{
				quad = tintQuad(quad, data.colorMultipliers[i]);
			}

			result.add(quad);
		}

		return result;
	}

	private static BakedQuad transformQuadFace(BakedQuad quad, Direction fromFace, Direction toFace)
	{
		int[] oldData = quad.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		for (int v = 0; v < 4; v++)
		{
			int off = v * stride;
			float x = Float.intBitsToFloat(oldData[off]);
			float y = Float.intBitsToFloat(oldData[off + 1]);
			float z = Float.intBitsToFloat(oldData[off + 2]);

			float[] uv = posToUV(fromFace, x, y, z);
			float[] newPos = uvToPos(toFace, uv[0], uv[1]);

			newData[off] = Float.floatToRawIntBits(newPos[0]);
			newData[off + 1] = Float.floatToRawIntBits(newPos[1]);
			newData[off + 2] = Float.floatToRawIntBits(newPos[2]);
		}

		return new BakedQuad(newData, quad.getTintIndex(), toFace, quad.getSprite(), quad.isShade());
	}

	private static float[] posToUV(Direction face, float x, float y, float z)
	{
		return switch (face)
		{
			case NORTH -> new float[] { x, y };
			case SOUTH -> new float[] { 1 - x, y };
			case WEST -> new float[] { 1 - z, y };
			case EAST -> new float[] { z, y };
			case DOWN -> new float[] { x, 1 - z };
			case UP -> new float[] { x, z };
		};
	}

	private static float[] uvToPos(Direction face, float u, float v)
	{
		return switch (face)
		{
			case NORTH -> new float[] { u, v, 0 };
			case SOUTH -> new float[] { 1 - u, v, 1 };
			case WEST -> new float[] { 0, v, 1 - u };
			case EAST -> new float[] { 1, v, u };
			case DOWN -> new float[] { u, 0, 1 - v };
			case UP -> new float[] { u, 1, v };
		};
	}

	private static BakedQuad tintQuad(BakedQuad quad, int color)
	{
		int[] oldData = quad.getVertices();
		int[] newData = Arrays.copyOf(oldData, oldData.length);
		int stride = oldData.length >>> 2;

		float r = ((color >>> 16) & 0xFF) / 255f;
		float g = ((color >>> 8) & 0xFF) / 255f;
		float b = (color & 0xFF) / 255f;

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

		return new BakedQuad(newData, quad.getTintIndex(), quad.getDirection(), quad.getSprite(), quad.isShade());
	}
}
