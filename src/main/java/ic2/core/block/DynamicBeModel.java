package ic2.core.block;

import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.LogCategory;

import java.util.*;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBeModel<T> implements UnbakedModel, BakedModel
{
	protected final Ic2TileEntityBlock block;
	private final ResourceLocation backingModelId;
	protected BakedModel baseModel;
	private BakedModel activeBaseModel;
	private final List<T> cache;
	private final int cacheSize;

	private final StampedLock cacheLock = new StampedLock();

	protected DynamicBeModel(ResourceLocation id)
	{
		ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().substring(id.getPath().lastIndexOf('/') + 1));
		Block block = ForgeRegistries.BLOCKS.getValue(blockId);
		if (!(block instanceof Ic2TileEntityBlock))
		{
			throw new IllegalArgumentException("invalid id: " + id);
		}

		this.block = (Ic2TileEntityBlock) block;
		this.backingModelId = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath().replace("block/be/", "block/"));
		this.cacheSize = (this.block.facingProperty != null ? 6 : 1) * (this.block.canActive() ? 2 : 1);
		this.cache = new ArrayList<>(cacheSize);
		for (int i = 0; i < cacheSize; i++) this.cache.add(null);
	}

	public @NotNull Collection<ResourceLocation> getDependencies()
	{
		return this.block.canActive() ? Arrays.asList(this.backingModelId, this.getActiveModelId()) : Collections.singletonList(this.backingModelId);
	}

	private ResourceLocation getActiveModelId()
	{
		return ResourceLocation.fromNamespaceAndPath(this.backingModelId.getNamespace(), this.backingModelId.getPath().concat("_active"));
	}

	public void resolveParents(@NotNull Function<ResourceLocation, UnbakedModel> resolver)
	{
		for (ResourceLocation id : this.getDependencies())
		{
			if (resolver.apply(id) == null)
			{
				IC2.log.warn(LogCategory.Resource, "Missing model %s", id);
			}
		}
	}

	public BakedModel bake(ModelBaker loader, @NotNull Function<Material, TextureAtlasSprite> textureGetter, @NotNull ModelState rotationContainer, @NotNull ResourceLocation modelId)
	{
		this.baseModel = loader.bake(this.backingModelId, rotationContainer, textureGetter);
		if (this.baseModel == null)
		{
			throw new IllegalStateException("missing model " + this.backingModelId);
		}

		if (this.block.canActive())
		{
			this.activeBaseModel = loader.bake(this.getActiveModelId(), rotationContainer, textureGetter);
			if (this.activeBaseModel == null)
			{
				throw new IllegalStateException("missing model " + this.getActiveModelId());
			}
		}

		return this;
	}

	public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, @NotNull RandomSource random)
	{
		return Collections.emptyList();
	}

	public boolean useAmbientOcclusion()
	{
		return true;
	}

	public boolean isGui3d()
	{
		return true;
	}

	public boolean usesBlockLight()
	{
		return true;
	}

	public boolean isCustomRenderer()
	{
		return false;
	}

	public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData modelData)
	{
		return this.baseModel.getParticleIcon(modelData);
	}

	public @NotNull ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	protected T getMesh(BlockState state, boolean active)
	{
		int idx;
		Direction facing;
		if (this.block.facingProperty != null)
		{
			facing = state.getValue(this.block.facingProperty);
			idx = facing.ordinal();
		} else
		{
			facing = Direction.NORTH;
			idx = 0;
		}

		if (active)
		{
			idx += this.cacheSize >>> 1;
		}

		long stamp = this.cacheLock.readLock();

		try
		{
			T ret = this.cache.get(idx);
			if (ret != null)
			{
				return ret;
			}
		} finally
		{
			this.cacheLock.unlock(stamp);
		}
		T ret = this.generateMesh(active ? this.activeBaseModel : this.baseModel, switch (facing)
		{
			case DOWN, EAST -> 1;
			case UP, WEST -> 3;
			case NORTH -> 0;
			case SOUTH -> 2;
		}, facing.getAxis() == Axis.Y);
		stamp = this.cacheLock.readLock();

		try
		{
			T prev = this.cache.get(idx);
			if (prev != null)
			{
				return prev;
			}

			long newStamp = this.cacheLock.tryConvertToWriteLock(stamp);
			if (newStamp != 0L)
			{
				stamp = newStamp;
			} else
			{
				this.cacheLock.unlock(stamp);
				stamp = this.cacheLock.writeLock();
			}

			prev = this.cache.get(idx);
			if (prev != null)
			{
				return prev;
			}

			this.cache.set(idx, ret);
			return ret;
		} finally
		{
			this.cacheLock.unlock(stamp);
		}
	}

	protected abstract T generateMesh(BakedModel var1, int var2, boolean var3);

	protected static BakedQuad rotateQuad(BakedQuad quad, int rot, boolean rotX)
	{
		rot &= 3;
		if (rot == 0)
		{
			return quad;
		}

		int[] data = quad.getVertices();
		int[] newData = Arrays.copyOf(data, data.length);
		int stride = data.length >>> 2;
		int offsetA;
		int offsetB;
		if (rotX)
		{
			offsetA = 2;
			offsetB = 1;
		} else
		{
			offsetA = 0;
			offsetB = 2;
		}

		for (int i = 0; i < 4; i++)
		{
			int offset = i * stride;
			float a = Float.intBitsToFloat(data[offset + offsetA]);
			float b = Float.intBitsToFloat(data[offset + offsetB]);
			float na;
			b = switch (rot)
			{
				case 1 ->
				{
					na = 1.0F - b;
					yield a;
				}
				case 2 ->
				{
					na = 1.0F - a;
					yield 1.0F - b;
				}
				case 3 ->
				{
					na = b;
					yield 1.0F - a;
				}
				default -> throw new IllegalStateException();
			};

			newData[offset + offsetA] = Float.floatToRawIntBits(na);
			newData[offset + offsetB] = Float.floatToRawIntBits(b);
		}

		return new BakedQuad(newData, quad.getTintIndex(), rotateFace(quad.getDirection(), rot, rotX), quad.getSprite(), quad.isShade());
	}

	protected static Direction rotateFace(Direction face, int count, boolean rotX)
	{
		count &= 3;
		if (rotX && face.getAxis() != Axis.X)
		{
			for (int i = 0; i < count; i++)
			{
				face = face.getClockWise(Axis.X);
			}
		} else if (!rotX && face.getAxis() != Axis.Y)
		{
			for (int i = 0; i < count; i++)
			{
				face = face.getClockWise();
			}
		}

		return face;
	}
}
