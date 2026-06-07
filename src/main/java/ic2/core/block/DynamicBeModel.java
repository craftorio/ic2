package ic2.core.block;

import com.mojang.datafixers.util.Pair;
import ic2.core.IC2;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.util.LogCategory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicBeModel<T> implements UnbakedModel, BakedModel
{
	protected final Ic2TileEntityBlock block;
	private final ResourceLocation backingModelId;
	private BakedModel baseModel;
	private BakedModel activeBaseModel;
	private final T[] cache;
	private final StampedLock cacheLock = new StampedLock();

	protected DynamicBeModel(ResourceLocation id)
	{
		ResourceLocation blockId = ResourceLocation.fromNamespaceAndPath(id.m_135827_(), id.m_135815_().substring(id.m_135815_().lastIndexOf(47) + 1));
		Block block = (Block) Registry.BLOCK.m_7745_(blockId);
		if (!(block instanceof Ic2TileEntityBlock))
		{
			throw new IllegalArgumentException("invalid id: " + id);
		}

		this.block = (Ic2TileEntityBlock) block;
		this.backingModelId = ResourceLocation.fromNamespaceAndPath(id.m_135827_(), id.m_135815_().replace("block/be/", "block/"));
		this.cache = (T[]) (new Object[(this.block.facingProperty != null ? 6 : 1) * (this.block.canActive() ? 2 : 1)]);
	}

	public Collection<ResourceLocation> m_7970_()
	{
		return this.block.canActive() ? Arrays.asList(this.backingModelId, this.getActiveModelId()) : Collections.singletonList(this.backingModelId);
	}

	private ResourceLocation getActiveModelId()
	{
		return ResourceLocation.fromNamespaceAndPath(this.backingModelId.m_135827_(), this.backingModelId.m_135815_().concat("_active"));
	}

	public Collection<Material> m_5500_(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
	{
		Set<Material> ret = new HashSet<>();

		for (ResourceLocation id : this.m_7970_())
		{
			UnbakedModel backingModel = unbakedModelGetter.apply(id);
			if (backingModel == null)
			{
				IC2.log.warn(LogCategory.Resource, "Missing model %s", id);
			} else
			{
				ret.addAll(backingModel.m_5500_(unbakedModelGetter, unresolvedTextureReferences));
			}
		}

		return ret;
	}

	public BakedModel m_7611_(ModelBakery loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId)
	{
		this.baseModel = loader.m_119349_(this.backingModelId, rotationContainer);
		if (this.baseModel == null)
		{
			throw new IllegalStateException("missing model " + this.backingModelId);
		}

		if (this.block.canActive())
		{
			this.activeBaseModel = loader.m_119349_(this.getActiveModelId(), rotationContainer);
			if (this.activeBaseModel == null)
			{
				throw new IllegalStateException("missing model " + this.getActiveModelId());
			}
		}

		return this;
	}

	public List<BakedQuad> m_213637_(@Nullable BlockState state, @Nullable Direction side, RandomSource random)
	{
		return Collections.emptyList();
	}

	public boolean m_7541_()
	{
		return true;
	}

	public boolean m_7539_()
	{
		return true;
	}

	public boolean m_7547_()
	{
		return true;
	}

	public boolean m_7521_()
	{
		return false;
	}

	public TextureAtlasSprite m_6160_()
	{
		return this.baseModel.m_6160_();
	}

	public ItemTransforms m_7442_()
	{
		return ItemTransforms.f_111786_;
	}

	public ItemOverrides m_7343_()
	{
		return ItemOverrides.f_111734_;
	}

	protected T getMesh(BlockState state, boolean active)
	{
		int idx;
		Direction facing;
		if (this.block.facingProperty != null)
		{
			facing = (Direction) state.getValue(this.block.facingProperty);
			idx = facing.ordinal();
		} else
		{
			facing = Direction.NORTH;
			idx = 0;
		}

		if (active)
		{
			idx += this.cache.length >>> 1;
		}

		long stamp = this.cacheLock.readLock();

		try
		{
			T ret = this.cache[idx];
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
			case DOWN -> 1;
			case UP -> 3;
			case NORTH -> 0;
			case SOUTH -> 2;
			case WEST -> 3;
			case EAST -> 1;
			default -> throw new IllegalStateException();
		}, facing.m_122434_() == Axis.Y);
		stamp = this.cacheLock.readLock();

		try
		{
			T prev = this.cache[idx];
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

			prev = this.cache[idx];
			if (prev != null)
			{
				return prev;
			}

			this.cache[idx] = ret;
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

		int[] data = quad.m_111303_();
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
			switch (rot)
			{
				case 1:
					na = 1.0F - b;
					b = a;
					break;
				case 2:
					na = 1.0F - a;
					b = 1.0F - b;
					break;
				case 3:
					na = b;
					b = 1.0F - a;
					break;
				default:
					throw new IllegalStateException();
			}

			newData[offset + offsetA] = Float.floatToRawIntBits(na);
			newData[offset + offsetB] = Float.floatToRawIntBits(b);
		}

		return new BakedQuad(newData, quad.m_111305_(), rotateFace(quad.m_111306_(), rot, rotX), quad.m_173410_(), quad.m_111307_());
	}

	protected static Direction rotateFace(Direction face, int count, boolean rotX)
	{
		count &= 3;
		if (rotX && face.m_122434_() != Axis.X)
		{
			for (int i = 0; i < count; i++)
			{
				face = face.m_175362_(Axis.X);
			}
		} else if (!rotX && face.m_122434_() != Axis.Y)
		{
			for (int i = 0; i < count; i++)
			{
				face = face.m_122427_();
			}
		}

		return face;
	}
}
