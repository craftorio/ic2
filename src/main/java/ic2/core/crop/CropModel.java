package ic2.core.crop;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ic2.api.crops.CropCard;
import ic2.api.crops.Crops;
import ic2.core.block.state.Ic2BlockState;
import ic2.core.model.AbstractModel;
import ic2.core.model.BasicBakedBlockModel;
import ic2.core.model.VdUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.model.IModelState;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class CropModel extends AbstractModel
{
	private static final ResourceLocation STICK = new ResourceLocation("ic2", "blocks/crop/stick");
	private static final ResourceLocation UPGRADED_STICK = new ResourceLocation("ic2", "blocks/crop/stick_upgraded");
	private static final Function<ResourceLocation, TextureAtlasSprite> MISSING = location -> Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite();
	static final Map<ResourceLocation, TextureAtlasSprite> textures = new HashMap<>();
	private final LoadingCache<TileEntityCrop.CropRenderState, IBakedModel> modelCache = CacheBuilder.newBuilder()
		.maximumSize(256L)
		.expireAfterAccess(5L, TimeUnit.MINUTES)
		.build(new CacheLoader<TileEntityCrop.CropRenderState, IBakedModel>()
		{
			public IBakedModel load(TileEntityCrop.CropRenderState key)
			{
				return key.crop != null && key.size > 0 ? CropModel.this.generateModel(key) : CropModel.this.generateStickModel(key.crosscrop);
			}
		});

	@Override
	public Collection<ResourceLocation> getTextures()
	{
		if (textures.isEmpty())
		{
			IC2Crops.needsToPost = false;
			MinecraftForge.EVENT_BUS.post(new Crops.CropRegisterEvent());

			for (CropCard crop : Crops.instance.getCrops())
			{
				for (ResourceLocation aux : crop.getTexturesLocation())
				{
					textures.put(aux, null);
				}
			}

			textures.put(STICK, null);
			textures.put(UPGRADED_STICK, null);
		}

		return textures.keySet();
	}

	@Override
	public IBakedModel bake(IModelState state, VertexFormat format, Function<ResourceLocation, TextureAtlasSprite> bakedTextureGetter)
	{
		for (Entry<ResourceLocation, TextureAtlasSprite> entry : textures.entrySet())
		{
			entry.setValue(bakedTextureGetter.apply(entry.getKey()));
		}

		return this;
	}

	private static ResourceLocation getTextureLocation(CropCard crop, int currentSize)
	{
		return crop.getTexturesLocation().get(currentSize - 1);
	}

	@Override
	public List<BakedQuad> getQuads(IBlockState rawState, EnumFacing side, long rand)
	{
		Ic2BlockState.Ic2BlockStateInstance state;
		TileEntityCrop.CropRenderState prop;
		if (rawState instanceof Ic2BlockState.Ic2BlockStateInstance
			&& (state = (Ic2BlockState.Ic2BlockStateInstance) rawState).hasValue(TileEntityCrop.renderStateProperty))
		{
			prop = state.getValue(TileEntityCrop.renderStateProperty);
		} else
		{
			prop = new TileEntityCrop.CropRenderState(null, 0, false);
		}

		try
		{
			return ((IBakedModel) this.modelCache.get(prop)).getQuads(rawState, side, rand);
		} catch (Exception error)
		{
			throw new RuntimeException(error);
		}
	}

	IBakedModel generateModel(TileEntityCrop.CropRenderState prop)
	{
		List<BakedQuad>[] faceQuads = new List[EnumFacing.HORIZONTALS.length];

		for (int index = 0; index < faceQuads.length; index++)
		{
			faceQuads[index] = new ArrayList<>();
		}

		List<BakedQuad> generalQuads = new ArrayList<>();
		TextureAtlasSprite cropSprite = textures.computeIfAbsent(getTextureLocation(prop.crop, prop.size), MISSING);

		for (EnumFacing facing : EnumFacing.HORIZONTALS)
		{
			int offsetX = facing.getFrontOffsetX();
			int offsetZ = facing.getFrontOffsetZ();
			float x = Math.abs(offsetX) * (0.5F + offsetX * 0.25F);
			float z = Math.abs(offsetZ) * (0.5F + offsetZ * 0.25F);
			float xS = offsetX == 0 ? 0.0F : x;
			float xE = offsetX == 0 ? 1.0F : x;
			float zS = offsetZ == 0 ? 0.0F : z;
			float zE = offsetZ == 0 ? 1.0F : z;
			VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), cropSprite, faceQuads, generalQuads, -0.0625F);
			VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.getOpposite()), cropSprite, faceQuads, generalQuads, -0.0625F);
		}

		int used = 0;

		for (int index = 0; index < faceQuads.length; index++)
		{
			if (faceQuads[index].isEmpty())
			{
				faceQuads[index] = Collections.emptyList();
			} else
			{
				used++;
			}
		}

		if (used == 0)
		{
			faceQuads = null;
		}

		if (generalQuads.isEmpty())
		{
			generalQuads = Collections.emptyList();
		}

		return new BasicBakedBlockModel(faceQuads, generalQuads, cropSprite);
	}

	IBakedModel generateStickModel(boolean crosscrop)
	{
		List<BakedQuad>[] faceQuads = new List[EnumFacing.HORIZONTALS.length];

		for (int index = 0; index < faceQuads.length; index++)
		{
			faceQuads[index] = new ArrayList<>();
		}

		List<BakedQuad> generalQuads = new ArrayList<>();
		TextureAtlasSprite stickSprite = textures.get(STICK);
		TextureAtlasSprite upgradedStickSprite = textures.get(UPGRADED_STICK);

		for (EnumFacing facing : EnumFacing.HORIZONTALS)
		{
			int offsetX = facing.getFrontOffsetX();
			int offsetZ = facing.getFrontOffsetZ();
			float x = Math.abs(offsetX) * (0.5F + offsetX * 0.25F);
			float z = Math.abs(offsetZ) * (0.5F + offsetZ * 0.25F);
			float xS = offsetX == 0 ? 0.0F : x;
			float xE = offsetX == 0 ? 1.0F : x;
			float zS = offsetZ == 0 ? 0.0F : z;
			float zE = offsetZ == 0 ? 1.0F : z;
			if (!crosscrop)
			{
				VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), stickSprite, faceQuads, generalQuads, -0.0625F);
				VdUtil.addFlippedCuboidWithYOffset(
					xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.getOpposite()), stickSprite, faceQuads, generalQuads, -0.0625F
				);
			} else
			{
				VdUtil.addFlippedCuboidWithYOffset(xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing), upgradedStickSprite, faceQuads, generalQuads, -0.0625F);
				VdUtil.addFlippedCuboidWithYOffset(
					xS, 0.001F, zS, xE, 1.0F, zE, -1, EnumSet.of(facing.getOpposite()), upgradedStickSprite, faceQuads, generalQuads, -0.0625F
				);
			}
		}

		int used = 0;

		for (int index = 0; index < faceQuads.length; index++)
		{
			if (faceQuads[index].isEmpty())
			{
				faceQuads[index] = Collections.emptyList();
			} else
			{
				used++;
			}
		}

		if (used == 0)
		{
			faceQuads = null;
		}

		if (generalQuads.isEmpty())
		{
			generalQuads = Collections.emptyList();
		}

		return new BasicBakedBlockModel(faceQuads, generalQuads, stickSprite);
	}

	@Override
	public TextureAtlasSprite getParticleTexture()
	{
		return textures.get(STICK);
	}

	@Override
	public void onReload()
	{
		this.modelCache.invalidateAll();
	}
}
