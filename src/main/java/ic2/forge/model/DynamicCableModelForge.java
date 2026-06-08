package ic2.forge.model;

import com.mojang.datafixers.util.Pair;
import ic2.core.block.wiring.CableFoam;
import ic2.core.block.wiring.CableType;
import ic2.core.block.wiring.DynamicCableModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.client.model.data.ModelProperty;
import net.minecraftforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.Nullable;

final class DynamicCableModelForge extends DynamicCableModel<List<BakedQuad>[], List<BakedQuad>[]> implements Ic2Model
{
	private static final ModelProperty<List<BakedQuad>[]> MESH_DATA = new ModelProperty<>();
	private static final int stride = 8;

	protected DynamicCableModelForge(CableType type, int insulation, CableFoam foam, boolean active)
	{
		super(type, insulation, foam, active);
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
		List<BakedQuad>[] mesh = this.getMesh(state);
		tileData = tileData.derive().with(MESH_DATA, mesh).build();
		assert tileData.get(MESH_DATA) == mesh;
		return tileData;
	}

	public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData extraData, @Nullable RenderType layer)
	{
		List<BakedQuad>[] mesh = extraData.get(MESH_DATA);
		return mesh[getIdx(side)];
	}

	protected List<BakedQuad>[] generateMesh(DyeColor color, int connections)
	{
		List<BakedQuad>[] mesh = new List[7];

		for (int i = 0; i < mesh.length; i++)
		{
			mesh[i] = new ArrayList<>();
		}

		this.generateQuads(color, connections, mesh);

		for (int i = 0; i < mesh.length; i++)
		{
			if (mesh[i].isEmpty())
			{
				mesh[i] = Collections.emptyList();
			}
		}

		return mesh;
	}

	protected void emitQuad(List<BakedQuad>[] emitter, Direction face, float left, float bottom, float right, float top, float depth, TextureAtlasSprite texture)
	{
		float xs;
		float ys;
		float zs;
		float xe;
		float ye;
		float ze;
		switch (face)
		{
			case DOWN:
				ye = depth;
				ys = depth;
				xs = left;
				zs = bottom;
				xe = right;
				ze = top;
				break;
			case UP:
				ys = ye = 1.0F - depth;
				xs = left;
				zs = bottom;
				xe = right;
				ze = top;
				break;
			case NORTH:
				ze = depth;
				zs = depth;
				xs = left;
				ys = bottom;
				xe = right;
				ye = top;
				break;
			case SOUTH:
				zs = ze = 1.0F - depth;
				xs = left;
				ys = bottom;
				xe = right;
				ye = top;
				break;
			case WEST:
				xe = depth;
				xs = depth;
				ys = bottom;
				zs = left;
				ye = top;
				ze = right;
				break;
			case EAST:
				xs = xe = 1.0F - depth;
				ys = bottom;
				zs = left;
				ye = top;
				ze = right;
				break;
			default:
				throw new IllegalStateException();
		}

		this.emitQuad(emitter, face, xs, ys, zs, xe, ye, ze, texture);
	}

	protected void emitQuad(List<BakedQuad>[] emitter, Direction face, float xs, float ys, float zs, float xe, float ye, float ze, TextureAtlasSprite texture)
	{
		int[] data = new int[32];
		float us = texture.getU0();
		float vs = texture.getV0();
		float ue = texture.getU1();
		float ve = texture.getV1();
		int normals = packNormals(face.getStepX(), face.getStepY(), face.getStepZ());
		float depth;
		switch (face)
		{
			case DOWN:
				depth = ys;
				vertex(0, xs, ys, ze, map(xs, us, ue), map(1.0F - ze, vs, ve), normals, data);
				vertex(1, xs, ys, zs, map(xs, us, ue), map(1.0F - zs, vs, ve), normals, data);
				vertex(2, xe, ys, zs, map(xe, us, ue), map(1.0F - zs, vs, ve), normals, data);
				vertex(3, xe, ys, ze, map(xe, us, ue), map(1.0F - ze, vs, ve), normals, data);
				break;
			case UP:
				depth = 1.0F - ye;
				vertex(0, xs, ye, zs, map(xs, us, ue), map(zs, vs, ve), normals, data);
				vertex(1, xs, ye, ze, map(xs, us, ue), map(ze, vs, ve), normals, data);
				vertex(2, xe, ye, ze, map(xe, us, ue), map(ze, vs, ve), normals, data);
				vertex(3, xe, ye, zs, map(xe, us, ue), map(zs, vs, ve), normals, data);
				break;
			case NORTH:
				depth = zs;
				vertex(0, xe, ye, zs, map(1.0F - xe, us, ue), map(1.0F - ye, vs, ve), normals, data);
				vertex(1, xe, ys, zs, map(1.0F - xe, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(2, xs, ys, zs, map(1.0F - xs, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(3, xs, ye, zs, map(1.0F - xs, us, ue), map(1.0F - ye, vs, ve), normals, data);
				break;
			case SOUTH:
				depth = 1.0F - ze;
				vertex(0, xs, ye, ze, map(xs, us, ue), map(1.0F - ye, vs, ve), normals, data);
				vertex(1, xs, ys, ze, map(xs, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(2, xe, ys, ze, map(xe, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(3, xe, ye, ze, map(xe, us, ue), map(1.0F - ye, vs, ve), normals, data);
				break;
			case WEST:
				depth = xs;
				vertex(0, xs, ye, zs, map(zs, us, ue), map(1.0F - ye, vs, ve), normals, data);
				vertex(1, xs, ys, zs, map(zs, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(2, xs, ys, ze, map(ze, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(3, xs, ye, ze, map(ze, us, ue), map(1.0F - ye, vs, ve), normals, data);
				break;
			case EAST:
				depth = 1.0F - xe;
				vertex(0, xe, ye, ze, map(1.0F - ze, us, ue), map(1.0F - ye, vs, ve), normals, data);
				vertex(1, xe, ys, ze, map(1.0F - ze, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(2, xe, ys, zs, map(1.0F - zs, us, ue), map(1.0F - ys, vs, ve), normals, data);
				vertex(3, xe, ye, zs, map(1.0F - zs, us, ue), map(1.0F - ye, vs, ve), normals, data);
				break;
			default:
				throw new IllegalStateException();
		}

		BakedQuad quad = new BakedQuad(data, -1, face, texture, true);
		Direction cullFace = Math.abs(depth) < 1.0E-5 ? face : null;
		emitter[getIdx(cullFace)].add(quad);
	}

	private static float map(float value, float start, float end)
	{
		return start + value * (end - start);
	}

	private static void vertex(int vertex, float x, float y, float z, float u, float v, int normals, int[] out)
	{
		int offset = vertex * 8;
		out[offset++] = Float.floatToRawIntBits(x);
		out[offset++] = Float.floatToRawIntBits(y);
		out[offset++] = Float.floatToRawIntBits(z);
		out[offset++] = -1;
		out[offset++] = Float.floatToRawIntBits(u);
		out[offset++] = Float.floatToRawIntBits(v);
		out[++offset] = normals;
	}

	private static int packNormals(float nx, float ny, float nz)
	{
		return mapFloatToByte(nx) | mapFloatToByte(ny) << 8 | mapFloatToByte(nz) << 16;
	}

	private static int mapFloatToByte(float f)
	{
		assert f >= -1.0F && f <= 1.0F;
		return Math.round(f * 127.0F) & 0xFF;
	}

	private static int getIdx(Direction dir)
	{
		return dir == null ? 6 : dir.ordinal();
	}
}
