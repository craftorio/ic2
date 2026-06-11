package ic2.forge.model;

import ic2.core.block.DynamicBeModel;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

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
		BlockEntity be;
		boolean active = this.block.canActive() && (be = world.getBlockEntity(pos)) instanceof Ic2TileEntity && ((Ic2TileEntity) be).getActive();
		List<List<BakedQuad>> mesh = this.getMesh(state, active);
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
		return null;
	}
}
