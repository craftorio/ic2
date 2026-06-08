package ic2.forge.model;

import com.mojang.datafixers.util.Pair;
import ic2.core.block.DynamicBeModel;
import ic2.core.block.tileentity.Ic2TileEntity;
import ic2.core.util.Util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
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
import org.jetbrains.annotations.Nullable;

final class DynamicBeModelForge extends DynamicBeModel<List<BakedQuad>[]> implements Ic2Model
{
	private static final ModelProperty<List<BakedQuad>[]> MESH_DATA = new ModelProperty<>();

	protected DynamicBeModelForge(ResourceLocation id)
	{
		super(id);
	}

	@Override
	public BakedModel bake(
		IGeometryBakingContext owner,
		ModelBakery bakery,
		Function<Material, TextureAtlasSprite> spriteGetter,
		ModelState modelTransform,
		ItemOverrides overrides,
		ResourceLocation modelLocation
	)
	{
		return this.bake(bakery, spriteGetter, modelTransform, modelLocation);
	}

	@Override
	public Collection<Material> getMaterials(
		IGeometryBakingContext owner, Function<ResourceLocation, UnbakedModel> modelGetter, Set<Pair<String, String>> missingTextureErrors
	)
	{
		return this.getMaterials(modelGetter, missingTextureErrors);
	}

	public ModelData getModelData(BlockAndTintGetter world, BlockPos pos, BlockState state, ModelData tileData)
	{
		BlockEntity be;
		boolean active = this.block.canActive() && (be = world.getBlockEntity(pos)) instanceof Ic2TileEntity && ((Ic2TileEntity) be).getActive();
		List<BakedQuad>[] mesh = this.getMesh(state, active);
		tileData = tileData.derive().with(MESH_DATA, mesh).build();
		assert tileData.get(MESH_DATA) == mesh;
		return tileData;
	}

	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType renderType)
	{
		List<BakedQuad>[] mesh = extraData.get(MESH_DATA);
		return mesh[getIdx(side)];
	}

	protected List<BakedQuad>[] generateMesh(BakedModel baseModel, int rot, boolean rotX)
	{
		List<BakedQuad>[] mesh = new List[7];

		for (int i = 0; i < mesh.length; i++)
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

			mesh[writeIdx] = quads;
		}

		return mesh;
	}

	private static int getIdx(Direction dir)
	{
		return dir == null ? 6 : dir.ordinal();
	}
}
