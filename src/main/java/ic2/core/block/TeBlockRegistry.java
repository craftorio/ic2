package ic2.core.block;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import ic2.api.event.TeBlockFinalCallEvent;
import ic2.api.item.ITeBlockSpecialItem;
import ic2.core.ref.BlockName;
import ic2.core.ref.TeBlock;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map.Entry;

import net.minecraft.block.material.Material;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;

public final class TeBlockRegistry
{
	private static final Map<String, ITeBlock> NAME_REGISTRY = new HashMap<>();
	private static final Map<Class<? extends TileEntityBlock>, ITeBlock> CLASS_REGISTRY = new IdentityHashMap<>();
	private static final Map<String, Class<? extends TileEntityBlock>> OLD_REGISTRY = new HashMap<>();
	private static final Map<ResourceLocation, TeBlockRegistry.TeBlockInfo<?>> RESOURCE_REGISTRY = new HashMap<>(5);
	private static boolean blocksBuilt;

	public static <E extends Enum<E> & ITeBlock> void add(E block)
	{
		if (!canBuildBlocks())
		{
			throw new IllegalStateException("Cannot register additional ITeBlocks once block map built!");
		}

		if (block == null)
		{
			throw new NullPointerException("Cannot register null ITeBlock!");
		}

		ResourceLocation loc = block.getIdentifier();
		TeBlockRegistry.TeBlockInfo<E> instance;
		if (!RESOURCE_REGISTRY.containsKey(loc))
		{
			RESOURCE_REGISTRY.put(loc, instance = new TeBlockRegistry.TeBlockInfo<>(block));
		} else
		{
			instance = (TeBlockRegistry.TeBlockInfo<E>) RESOURCE_REGISTRY.get(loc);
		}

		instance.register(block);
		if (block instanceof ITeBlock.ITeBlockCreativeRegisterer)
		{
			instance.setCreativeRegisterer((ITeBlock.ITeBlockCreativeRegisterer) block);
		}
	}

	public static <E extends Enum<E> & ITeBlock> void addAll(Class<E> enumClass, ResourceLocation identifier)
	{
		if (!canBuildBlocks())
		{
			throw new IllegalStateException("Cannot register additional ITeBlocks once block map built!");
		}

		if (EnumSet.allOf(enumClass).isEmpty())
		{
			throw new IllegalArgumentException("Cannot register empty enum!");
		}

		if (identifier == null)
		{
			throw new NullPointerException("Cannot register a null identifier!");
		}

		if (RESOURCE_REGISTRY.containsKey(identifier))
		{
			throw new IllegalArgumentException("Already registered an enum for " + identifier);
		}

		TeBlockRegistry.TeBlockInfo<E> instance = new TeBlockRegistry.TeBlockInfo<>(enumClass);
		RESOURCE_REGISTRY.put(identifier, instance);
		instance.registerAll(enumClass);
	}

	public static <T extends ITeBlock & ITeBlock.ITeBlockCreativeRegisterer> void addCreativeRegisterer(T registerer)
	{
		addCreativeRegisterer(registerer, registerer.getIdentifier());
	}

	public static void addCreativeRegisterer(ITeBlock.ITeBlockCreativeRegisterer registerer, ResourceLocation identifier)
	{
		if (!RESOURCE_REGISTRY.containsKey(identifier))
		{
			throw new IllegalStateException("Must register an ITeBlock instance before adding a creative registerer!");
		}

		RESOURCE_REGISTRY.get(identifier).setCreativeRegisterer(registerer);
	}

	public static void setDefaultMaterial(ResourceLocation identifier, Material material)
	{
		if (!RESOURCE_REGISTRY.containsKey(identifier))
		{
			throw new IllegalStateException("Must register an ITeBlock instance before setting the default material!");
		}

		RESOURCE_REGISTRY.get(identifier).setDefaultMaterial(material);
	}

	static void addName(ITeBlock teBlock)
	{
		if (NAME_REGISTRY.put(teBlock.getName(), teBlock) != null)
		{
			throw new IllegalStateException("Duplicate name for different ITeBlocks!");
		}
	}

	static void addClass(ITeBlock teBlock)
	{
		if (CLASS_REGISTRY.put(teBlock.getTeClass(), teBlock) != null)
		{
			throw new IllegalStateException("Duplicate class name for different ITeBlocks!");
		}
	}

	public static void ensureMapping(TeBlock block, Class<? extends TileEntityBlock> te)
	{
		CLASS_REGISTRY.putIfAbsent(te, block);
		if (block.getTeClass() != te)
		{
			OLD_REGISTRY.put("Old-" + block.getName(), te);
		}
	}

	public static void buildBlocks()
	{
		if (!canBuildBlocks())
		{
			throw new IllegalStateException("Cannot build blocks twice!");
		}

		MinecraftForge.EVENT_BUS.post(new TeBlockFinalCallEvent());
		blocksBuilt = true;
		ResourceLocation ic2Loc = TeBlock.invalid.getIdentifier();

		for (Entry<ResourceLocation, TeBlockRegistry.TeBlockInfo<?>> entry : RESOURCE_REGISTRY.entrySet())
		{
			ResourceLocation location = entry.getKey();
			TeBlockRegistry.TeBlockInfo<?> info = entry.getValue();
			Set<Material> mats = new LinkedHashSet<>();
			mats.add(info.getDefaultMaterial());

			for (ITeBlock teBlock : info.getTeBlocks())
			{
				mats.add(teBlock.getMaterial());
			}

			if (mats.size() > 8)
			{
				throw new RuntimeException("Cannot form a TeBlock with more than 8 different materials (attempted " + mats.size() + ')');
			}

			BlockTileEntity block;
			if (location == ic2Loc)
			{
				block = BlockTileEntity.create(BlockName.te, mats);
			} else
			{
				block = BlockTileEntity.create("te_" + location.getResourcePath(), location, mats);
			}

			info.setBlock(block);
		}
	}

	public static boolean canBuildBlocks()
	{
		return !blocksBuilt;
	}

	public static ITeBlock get(String name)
	{
		ITeBlock ret = NAME_REGISTRY.get(name);
		return ret != null ? ret : TeBlock.invalid;
	}

	public static Class<? extends TileEntityBlock> getOld(String name)
	{
		return OLD_REGISTRY.get(name);
	}

	public static ITeBlock get(ResourceLocation identifier, int ID)
	{
		if (ID >= 0 && RESOURCE_REGISTRY.containsKey(identifier))
		{
			List<ITeBlock> items = RESOURCE_REGISTRY.get(identifier).getIdMap();
			if (ID < items.size())
			{
				return items.get(ID);
			}
		}

		return null;
	}

	public static ITeBlock get(Class<? extends TileEntityBlock> cls)
	{
		return CLASS_REGISTRY.get(cls);
	}

	public static BlockTileEntity get(ResourceLocation identifier)
	{
		return RESOURCE_REGISTRY.containsKey(identifier) ? RESOURCE_REGISTRY.get(identifier).getBlock() : null;
	}

	public static Iterable<Entry<ResourceLocation, Set<? extends ITeBlock>>> getAll()
	{
		return Collections2.transform(
			RESOURCE_REGISTRY.entrySet(),
			new Function<Entry<ResourceLocation, TeBlockRegistry.TeBlockInfo<?>>, Entry<ResourceLocation, Set<? extends ITeBlock>>>()
			{
				public SimpleImmutableEntry<ResourceLocation, Set<? extends ITeBlock>> apply(Entry<ResourceLocation, TeBlockRegistry.TeBlockInfo<?>> input)
				{
					return new SimpleImmutableEntry<>(input.getKey(), input.getValue().getTeBlocks());
				}
			}
		);
	}

	public static Collection<BlockTileEntity> getAllBlocks()
	{
		return Collections2.transform(RESOURCE_REGISTRY.values(), new Function<TeBlockRegistry.TeBlockInfo<?>, BlockTileEntity>()
		{
			public BlockTileEntity apply(TeBlockRegistry.TeBlockInfo<?> input)
			{
				return input.getBlock();
			}
		});
	}

	public static Set<? extends ITeBlock> getAll(ResourceLocation identifier)
	{
		return RESOURCE_REGISTRY.containsKey(identifier) ? RESOURCE_REGISTRY.get(identifier).getTeBlocks() : Collections.emptySet();
	}

	static TeBlockRegistry.TeBlockInfo<?> getInfo(ResourceLocation identifier)
	{
		return RESOURCE_REGISTRY.get(identifier);
	}

	static List<ITeBlock> getItems(ResourceLocation identifier)
	{
		return RESOURCE_REGISTRY.containsKey(identifier) ? RESOURCE_REGISTRY.get(identifier).getIdMap() : Collections.emptyList();
	}

	private TeBlockRegistry()
	{
	}

	public static class TeBlockInfo<E extends Enum<E> & ITeBlock>
	{
		private BlockTileEntity block;
		private final boolean specialModels;
		private Material defaultMaterial = Material.IRON;
		private ITeBlock.ITeBlockCreativeRegisterer creativeRegisterer;
		private final Set<E> teBlocks;
		private final List<ITeBlock> idMap = new ArrayList<>();

		TeBlockInfo(E universe)
		{
			this((Class<E>) universe.getClass());
		}

		TeBlockInfo(Class<E> universe)
		{
			this.teBlocks = EnumSet.noneOf(universe);
			this.specialModels = ITeBlockSpecialItem.class.isAssignableFrom(universe);
		}

		void register(E block)
		{
			if (!this.teBlocks.add(block))
			{
				throw new IllegalStateException("ITeBlock already registered!");
			}

			TeBlockRegistry.addName(block);
			TeBlockRegistry.addClass(block);
			if (block.getId() > -1)
			{
				int ID = block.getId();

				while (this.idMap.size() < ID)
				{
					this.idMap.add(null);
				}

				if (this.idMap.size() == ID)
				{
					this.idMap.add(block);
				} else
				{
					if (this.idMap.get(ID) != null)
					{
						throw new IllegalStateException("The id " + ID + " for " + block + " is already in use by " + this.idMap.get(ID) + '.');
					}

					this.idMap.set(ID, block);
				}
			}
		}

		void registerAll(Class<E> universe)
		{
			for (E block : EnumSet.allOf(universe))
			{
				this.register(block);
			}
		}

		void setBlock(BlockTileEntity block)
		{
			if (this.hasBlock())
			{
				throw new IllegalStateException("Already has block set (" + this.block + ") when adding " + block);
			}

			this.block = block;
		}

		public boolean hasBlock()
		{
			return this.block != null;
		}

		public BlockTileEntity getBlock()
		{
			return this.block;
		}

		void setCreativeRegisterer(ITeBlock.ITeBlockCreativeRegisterer creativeRegisterer)
		{
			this.creativeRegisterer = creativeRegisterer;
		}

		public boolean hasCreativeRegisterer()
		{
			return this.creativeRegisterer != null;
		}

		public ITeBlock.ITeBlockCreativeRegisterer getCreativeRegisterer()
		{
			return this.creativeRegisterer;
		}

		void setDefaultMaterial(Material material)
		{
			this.defaultMaterial = material;
		}

		public Material getDefaultMaterial()
		{
			return this.defaultMaterial;
		}

		public boolean hasSpecialModels()
		{
			return this.specialModels;
		}

		public Set<? extends ITeBlock> getTeBlocks()
		{
			return Collections.unmodifiableSet(this.teBlocks);
		}

		public List<ITeBlock> getIdMap()
		{
			return Collections.unmodifiableList(this.idMap);
		}
	}
}
