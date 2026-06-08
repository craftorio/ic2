package ic2.core.block.wiring;

import com.mojang.datafixers.util.Pair;
import ic2.core.IC2;
import ic2.core.util.Util;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Function;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.UnbakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class DynamicCableModel<T, E> implements UnbakedModel, BakedModel
{
	private final CableType type;
	private final int insulation;
	private final CableFoam foam;
	private final boolean active;
	private Map<DyeColor, TextureAtlasSprite> sprites;
	private TextureAtlasSprite blackSprite;
	private TextureAtlasSprite particleTexture;
	private final Int2ObjectMap<T> cache = new Int2ObjectOpenHashMap<>();
	private final StampedLock cacheLock = new StampedLock();

	protected DynamicCableModel(CableType type, int insulation, CableFoam foam, boolean active)
	{
		this.type = type;
		this.insulation = insulation;
		this.foam = foam;
		this.active = active;
	}

	public Collection<ResourceLocation> getDependencies()
	{
		return Collections.emptyList();
	}

	public void resolveParents(Function<ResourceLocation, UnbakedModel> resolver)
	{
	}

	public Collection<Material> getMaterials(Function<ResourceLocation, UnbakedModel> unbakedModelGetter, Set<Pair<String, String>> unresolvedTextureReferences)
	{
		if (this.insulation < this.type.minColoredInsulation)
		{
			return Collections.singletonList(getTextureId(this.type, this.insulation, DyeColor.BLACK, this.active));
		}

		List<Material> ret = new ArrayList<>(16);

		for (DyeColor color : DyeColor.values())
		{
			ret.add(getTextureId(this.type, this.insulation, color, false));
		}

		return ret;
	}

	private static Material getTextureId(CableType type, int insulation, DyeColor color, boolean active)
	{
		StringBuilder sb = new StringBuilder(50);
		sb.append("blocks/wiring/cable/");
		sb.append(type.name());
		sb.append("_cable_");
		sb.append(insulation);
		if (insulation >= type.minColoredInsulation)
		{
			sb.append('_');
			sb.append(color.getSerializedName());
		}

		if (active && (type == CableType.detector || type == CableType.splitter))
		{
			sb.append("_active");
		}

		return getTextureId(sb.toString());
	}

	private static Material getTextureId(String path)
	{
		ResourceLocation atlas = TextureAtlas.LOCATION_BLOCKS;
		return new Material(atlas, IC2.getIdentifier(path));
	}

	public BakedModel bake(ModelBaker loader, Function<Material, TextureAtlasSprite> textureGetter, ModelState rotationContainer, ResourceLocation modelId)
	{
		this.blackSprite = textureGetter.apply(getTextureId(this.type, this.insulation, DyeColor.BLACK, this.active));
		if (!this.foam.isPresent())
		{
			this.particleTexture = this.blackSprite;
		} else if (this.foam.isSoft())
		{
			this.particleTexture = textureGetter.apply(getTextureId("blocks/cf/foam"));
		} else
		{
			this.particleTexture = textureGetter.apply(getTextureId("blocks/cf/wall_".concat(this.foam.getColor().getSerializedName())));
		}

		if (this.insulation >= this.type.minColoredInsulation)
		{
			this.sprites = new EnumMap<>(DyeColor.class);

			for (DyeColor color : DyeColor.values())
			{
				TextureAtlasSprite sprite;
				if (color == DyeColor.BLACK)
				{
					sprite = this.blackSprite;
				} else
				{
					sprite = textureGetter.apply(getTextureId(this.type, this.insulation, color, this.active));
				}

				this.sprites.put(color, sprite);
			}
		}

		return this;
	}

	public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random)
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

	public TextureAtlasSprite getParticleIcon()
	{
		return this.particleTexture;
	}

	public ItemTransforms getTransforms()
	{
		return ItemTransforms.NO_TRANSFORMS;
	}

	public ItemOverrides getOverrides()
	{
		return ItemOverrides.EMPTY;
	}

	protected T getMesh(BlockState state)
	{
		DyeColor color = CableBlock.getColor(state, this.type, this.insulation);
		int connections = 0;
		int key = color.ordinal() << 6 | connections;
		long stamp = this.cacheLock.readLock();

		try
		{
			T ret = (T) this.cache.get(key);
			if (ret != null)
			{
				return ret;
			}
		} finally
		{
			this.cacheLock.unlock(stamp);
		}

		Object var20 = this.generateMesh(color, connections);
		stamp = this.cacheLock.readLock();

		try
		{
			T prev = (T) this.cache.get(key);
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

			prev = this.cache.putIfAbsent(key, (T) var20);
			return (T) (prev != null ? prev : var20);
		} finally
		{
			this.cacheLock.unlock(stamp);
		}
	}

	protected abstract T generateMesh(DyeColor var1, int var2);

	protected void generateQuads(DyeColor color, int connections, E emitter)
	{
		TextureAtlasSprite sprite = color == DyeColor.BLACK ? this.blackSprite : this.sprites.get(color);
		float th = this.type.getThickness(this.insulation);
		float sp = (1.0F - th) / 2.0F;
		float spth = sp + th;

		for (Direction facing : Util.ALL_DIRS)
		{
			boolean hasConnection = (connections & 1 << facing.ordinal()) != 0;
			if (hasConnection)
			{
				this.emitQuad(emitter, facing, sp, sp, spth, spth, 0.0F, sprite);
				if (!this.foam.isPresent())
				{
					float zS = sp;
					float yS = sp;
					float xS = sp;
					float zE = spth;
					float yE = spth;
					float xE = spth;
					switch (facing)
					{
						case DOWN:
							yS = 0.0F;
							yE = sp;
							break;
						case UP:
							yS = spth;
							yE = 1.0F;
							break;
						case NORTH:
							zS = 0.0F;
							zE = sp;
							break;
						case SOUTH:
							zS = spth;
							zE = 1.0F;
							break;
						case WEST:
							xS = 0.0F;
							xE = sp;
							break;
						case EAST:
							xS = spth;
							xE = 1.0F;
							break;
						default:
							throw new RuntimeException();
					}

					for (Direction side : Util.ALL_DIRS)
					{
						if (side.getAxis() != facing.getAxis())
						{
							this.emitQuad(emitter, side, xS, yS, zS, xE, yE, zE, sprite);
						}
					}
				} else
				{
					this.emitQuad(emitter, facing, 0.0F, 0.0F, sp, 1.0F, 0.0F, this.particleTexture);
					this.emitQuad(emitter, facing, spth, 0.0F, 1.0F, 1.0F, 0.0F, this.particleTexture);
					this.emitQuad(emitter, facing, sp, 0.0F, spth, sp, 0.0F, this.particleTexture);
					this.emitQuad(emitter, facing, sp, spth, spth, 1.0F, 0.0F, this.particleTexture);
				}
			} else if (!this.foam.isPresent())
			{
				this.emitQuad(emitter, facing, sp, sp, spth, spth, sp, sprite);
			} else
			{
				this.emitQuad(emitter, facing, 0.0F, 0.0F, 1.0F, 1.0F, 0.0F, this.particleTexture);
			}
		}
	}

	protected abstract void emitQuad(E var1, Direction var2, float var3, float var4, float var5, float var6, float var7, TextureAtlasSprite var8);

	protected abstract void emitQuad(E var1, Direction var2, float var3, float var4, float var5, float var6, float var7, float var8, TextureAtlasSprite var9);
}
