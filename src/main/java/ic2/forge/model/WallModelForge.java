package ic2.forge.model;

import ic2.core.block.comp.Obscuration;
import ic2.core.block.misc.WallBlock;
import ic2.core.block.tileentity.TileEntityWall;
import ic2.core.item.tool.ItemObscurator;

import java.util.ArrayList;
import java.util.Collections;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class WallModelForge implements Ic2Model
{
	private static final float OVERLAY_OFFSET = 0.001f;
	private static final ModelProperty<List<BakedQuad>[]> MESH_DATA = new ModelProperty<>();
	private static final ModelProperty<TextureAtlasSprite> PARTICLE_DATA = new ModelProperty<>();

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
		return new BakedWallModel();
	}

	private static List<BakedQuad>[] buildDefaultMesh(DyeColor color)
	{
		Block wallBlock = WallBlock.get(color);
		if (wallBlock == null)
		{
			wallBlock = WallBlock.get(WallBlock.DEFAULT_COLOR);
		}

		return buildMeshForState(wallBlock.defaultBlockState(), null);
	}

	private static List<BakedQuad>[] buildMesh(BlockAndTintGetter world, BlockPos pos, BlockState state, TileEntityWall wall)
	{
		DyeColor color = wall.getColor();
		Block wallBlock = WallBlock.get(color);
		if (wallBlock == null)
		{
			wallBlock = WallBlock.get(WallBlock.DEFAULT_COLOR);
		}

		BlockState baseState = wallBlock.defaultBlockState();
		Obscuration obscuration = wall.getComponent(Obscuration.class);
		Obscuration.ObscurationData[] obscurations = obscuration != null ? obscuration.getRenderState() : null;
		return buildMeshForState(baseState, obscurations);
	}

	private static List<BakedQuad>[] buildMeshForState(BlockState baseState, Obscuration.ObscurationData[] obscurations)
	{
		BakedModel baseModel = Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(baseState);
		RandomSource rand = RandomSource.create(42L);
		@SuppressWarnings("unchecked")
		List<BakedQuad>[] mesh = new List[6];

		for (Direction side : Direction.values())
		{
			List<BakedQuad> faceQuads = new ArrayList<>(baseModel.getQuads(baseState, side, rand, ModelData.EMPTY, null));
			Obscuration.ObscurationData data = obscurations != null ? obscurations[side.ordinal()] : null;
			if (data != null)
			{
				ItemObscurator.ObscuredRenderInfo renderInfo = ItemObscurator.getRenderInfo(data.state(), data.side());
				if (renderInfo != null && renderInfo.uvs.length == data.colorMultipliers().length * 4)
				{
					for (int texture = 0; texture < data.colorMultipliers().length; texture++)
					{
						BakedQuad overlay = OverlayRenderUtil.applyBlockOverlay(
							OverlayRenderUtil.createBlockOverlayQuad(side, OVERLAY_OFFSET),
							renderInfo,
							texture,
							data.colorMultipliers()[texture],
							side
						);
						faceQuads.add(overlay);
					}
				}
			}

			mesh[side.ordinal()] = faceQuads.isEmpty() ? Collections.emptyList() : faceQuads;
		}

		return mesh;
	}

	private static TextureAtlasSprite resolveParticleIcon(TileEntityWall wall)
	{
		Obscuration obscuration = wall.getComponent(Obscuration.class);
		Obscuration.ObscurationData[] obscurations = obscuration != null ? obscuration.getRenderState() : null;
		Obscuration.ObscurationData topData = obscurations != null ? obscurations[Direction.UP.ordinal()] : null;
		if (topData != null)
		{
			ItemObscurator.ObscuredRenderInfo renderInfo = ItemObscurator.getRenderInfo(topData.state(), topData.side());
			if (renderInfo != null && renderInfo.sprites.length > 0)
			{
				return renderInfo.sprites[0];
			}
		}

		Block wallBlock = WallBlock.get(wall.getColor());
		return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(wallBlock.defaultBlockState()).getParticleIcon();
	}

	private static final class BakedWallModel implements BakedModel
	{
		@Override
		public @NotNull List<BakedQuad> getQuads(
			@Nullable BlockState state,
			@Nullable Direction side,
			@NotNull RandomSource random,
			@NotNull ModelData extraData,
			@Nullable RenderType renderType
		)
		{
			if (side == null)
			{
				return Collections.emptyList();
			}

			List<BakedQuad>[] mesh = extraData.get(MESH_DATA);
			if (mesh == null)
			{
				return Collections.emptyList();
			}

			List<BakedQuad> quads = mesh[side.ordinal()];
			return quads != null ? quads : Collections.emptyList();
		}

		@Override
		public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random)
		{
			return this.getQuads(state, side, random, ModelData.EMPTY, null);
		}

		@Override
		public boolean useAmbientOcclusion()
		{
			return true;
		}

		@Override
		public boolean isGui3d()
		{
			return true;
		}

		@Override
		public boolean usesBlockLight()
		{
			return true;
		}

		@Override
		public boolean isCustomRenderer()
		{
			return false;
		}

		@Override
		public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
		{
			TextureAtlasSprite sprite = modelData.get(PARTICLE_DATA);
			return sprite != null ? sprite : this.getParticleIcon();
		}

		@Override
		public @NotNull TextureAtlasSprite getParticleIcon()
		{
			return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getBlockModel(WallBlock.get(WallBlock.DEFAULT_COLOR).defaultBlockState()).getParticleIcon();
		}

		@Override
		public @NotNull ItemOverrides getOverrides()
		{
			return ItemOverrides.EMPTY;
		}

		@Override
		public @NotNull ModelData getModelData(@NotNull BlockAndTintGetter world, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull ModelData tileData)
		{
			if (world.getBlockEntity(pos) instanceof TileEntityWall wall)
			{
				return tileData.derive()
					.with(MESH_DATA, buildMesh(world, pos, state, wall))
					.with(PARTICLE_DATA, resolveParticleIcon(wall))
					.build();
			}

			return tileData.derive()
				.with(MESH_DATA, buildDefaultMesh(WallBlock.DEFAULT_COLOR))
				.build();
		}
	}
}