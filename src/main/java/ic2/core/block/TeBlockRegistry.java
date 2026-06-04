// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.state.IIdProvider;
import ic2.api.item.ITeBlockSpecialItem;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.HashMap;
import java.util.Collections;
import com.google.common.collect.Collections2;
import java.util.AbstractMap;
import com.google.common.base.Function;
import java.util.List;
import java.util.Set;
import java.util.Iterator;
import java.util.Collection;
import ic2.core.ref.BlockName;
import java.util.LinkedHashSet;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.event.TeBlockFinalCallEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.core.ref.TeBlock;
import net.minecraft.block.material.Material;
import java.util.EnumSet;
import net.minecraft.util.ResourceLocation;
import java.util.Map;

public final class TeBlockRegistry
{
    private static final Map<String, ITeBlock> NAME_REGISTRY;
    private static final Map<Class<? extends TileEntityBlock>, ITeBlock> CLASS_REGISTRY;
    private static final Map<String, Class<? extends TileEntityBlock>> OLD_REGISTRY;
    private static final Map<ResourceLocation, TeBlockInfo<?>> RESOURCE_REGISTRY;
    private static boolean blocksBuilt;
    
    public static <E extends Enum<E> & ITeBlock> void add(final E block) {
        if (!canBuildBlocks()) {
            throw new IllegalStateException("Cannot register additional ITeBlocks once block map built!");
        }
        if (block == null) {
            throw new NullPointerException("Cannot register null ITeBlock!");
        }
        final ResourceLocation loc = block.getIdentifier();
        TeBlockInfo<E> instance;
        if (!TeBlockRegistry.RESOURCE_REGISTRY.containsKey(loc)) {
            TeBlockRegistry.RESOURCE_REGISTRY.put(loc, instance = new TeBlockInfo<E>(block));
        }
        else {
            instance = (TeBlockInfo)TeBlockRegistry.RESOURCE_REGISTRY.get(loc);
        }
        instance.register(block);
        if (block instanceof ITeBlock.ITeBlockCreativeRegisterer) {
            instance.setCreativeRegisterer((ITeBlock.ITeBlockCreativeRegisterer)block);
        }
    }
    
    public static <E extends Enum<E> & ITeBlock> void addAll(final Class<E> enumClass, final ResourceLocation identifier) {
        if (!canBuildBlocks()) {
            throw new IllegalStateException("Cannot register additional ITeBlocks once block map built!");
        }
        if (EnumSet.allOf(enumClass).isEmpty()) {
            throw new IllegalArgumentException("Cannot register empty enum!");
        }
        if (identifier == null) {
            throw new NullPointerException("Cannot register a null identifier!");
        }
        if (TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier)) {
            throw new IllegalArgumentException("Already registered an enum for " + identifier);
        }
        final TeBlockInfo<E> instance = new TeBlockInfo<E>(enumClass);
        TeBlockRegistry.RESOURCE_REGISTRY.put(identifier, instance);
        instance.registerAll(enumClass);
    }
    
    public static <T extends ITeBlock & ITeBlock.ITeBlockCreativeRegisterer> void addCreativeRegisterer(final T registerer) {
        addCreativeRegisterer(registerer, registerer.getIdentifier());
    }
    
    public static void addCreativeRegisterer(final ITeBlock.ITeBlockCreativeRegisterer registerer, final ResourceLocation identifier) {
        if (!TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier)) {
            throw new IllegalStateException("Must register an ITeBlock instance before adding a creative registerer!");
        }
        TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).setCreativeRegisterer(registerer);
    }
    
    public static void setDefaultMaterial(final ResourceLocation identifier, final Material material) {
        if (!TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier)) {
            throw new IllegalStateException("Must register an ITeBlock instance before setting the default material!");
        }
        TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).setDefaultMaterial(material);
    }
    
    static void addName(final ITeBlock teBlock) {
        if (TeBlockRegistry.NAME_REGISTRY.put(teBlock.getName(), teBlock) != null) {
            throw new IllegalStateException("Duplicate name for different ITeBlocks!");
        }
    }
    
    static void addClass(final ITeBlock teBlock) {
        if (TeBlockRegistry.CLASS_REGISTRY.put(teBlock.getTeClass(), teBlock) != null) {
            throw new IllegalStateException("Duplicate class name for different ITeBlocks!");
        }
    }
    
    public static void ensureMapping(final TeBlock block, final Class<? extends TileEntityBlock> te) {
        TeBlockRegistry.CLASS_REGISTRY.putIfAbsent(te, block);
        if (block.getTeClass() != te) {
            TeBlockRegistry.OLD_REGISTRY.put("Old-" + block.getName(), te);
        }
    }
    
    public static void buildBlocks() {
        if (!canBuildBlocks()) {
            throw new IllegalStateException("Cannot build blocks twice!");
        }
        MinecraftForge.EVENT_BUS.post((Event)new TeBlockFinalCallEvent());
        TeBlockRegistry.blocksBuilt = true;
        final ResourceLocation ic2Loc = TeBlock.invalid.getIdentifier();
        for (final Map.Entry<ResourceLocation, TeBlockInfo<?>> entry : TeBlockRegistry.RESOURCE_REGISTRY.entrySet()) {
            final ResourceLocation location = entry.getKey();
            final TeBlockInfo<?> info = entry.getValue();
            final Set<Material> mats = new LinkedHashSet<Material>();
            mats.add(info.getDefaultMaterial());
            for (final ITeBlock teBlock : info.getTeBlocks()) {
                mats.add(teBlock.getMaterial());
            }
            if (mats.size() > 8) {
                throw new RuntimeException("Cannot form a TeBlock with more than 8 different materials (attempted " + mats.size() + ')');
            }
            BlockTileEntity block;
            if (location == ic2Loc) {
                block = BlockTileEntity.create(BlockName.te, mats);
            }
            else {
                block = BlockTileEntity.create("te_" + location.getResourcePath(), location, mats);
            }
            info.setBlock(block);
        }
    }
    
    public static boolean canBuildBlocks() {
        return !TeBlockRegistry.blocksBuilt;
    }
    
    public static ITeBlock get(final String name) {
        final ITeBlock ret = TeBlockRegistry.NAME_REGISTRY.get(name);
        return (ret != null) ? ret : TeBlock.invalid;
    }
    
    public static Class<? extends TileEntityBlock> getOld(final String name) {
        return TeBlockRegistry.OLD_REGISTRY.get(name);
    }
    
    public static ITeBlock get(final ResourceLocation identifier, final int ID) {
        if (ID >= 0 && TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier)) {
            final List<ITeBlock> items = TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).getIdMap();
            if (ID < items.size()) {
                return items.get(ID);
            }
        }
        return null;
    }
    
    public static ITeBlock get(final Class<? extends TileEntityBlock> cls) {
        return TeBlockRegistry.CLASS_REGISTRY.get(cls);
    }
    
    public static BlockTileEntity get(final ResourceLocation identifier) {
        return TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier) ? TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).getBlock() : null;
    }
    
    public static Iterable<Map.Entry<ResourceLocation, Set<? extends ITeBlock>>> getAll() {
        return Collections2.transform((Collection)TeBlockRegistry.RESOURCE_REGISTRY.entrySet(), (Function)new Function<Map.Entry<ResourceLocation, TeBlockInfo<?>>, Map.Entry<ResourceLocation, Set<? extends ITeBlock>>>() {
            public AbstractMap.SimpleImmutableEntry<ResourceLocation, Set<? extends ITeBlock>> apply(final Map.Entry<ResourceLocation, TeBlockInfo<?>> input) {
                return new AbstractMap.SimpleImmutableEntry<ResourceLocation, Set<? extends ITeBlock>>(input.getKey(), input.getValue().getTeBlocks());
            }
        });
    }
    
    public static Collection<BlockTileEntity> getAllBlocks() {
        return Collections2.transform((Collection)TeBlockRegistry.RESOURCE_REGISTRY.values(), (Function)new Function<TeBlockInfo<?>, BlockTileEntity>() {
            public BlockTileEntity apply(final TeBlockInfo<?> input) {
                return input.getBlock();
            }
        });
    }
    
    public static Set<? extends ITeBlock> getAll(final ResourceLocation identifier) {
        return TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier) ? TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).getTeBlocks() : Collections.emptySet();
    }
    
    static TeBlockInfo<?> getInfo(final ResourceLocation identifier) {
        return TeBlockRegistry.RESOURCE_REGISTRY.get(identifier);
    }
    
    static List<ITeBlock> getItems(final ResourceLocation identifier) {
        return TeBlockRegistry.RESOURCE_REGISTRY.containsKey(identifier) ? TeBlockRegistry.RESOURCE_REGISTRY.get(identifier).getIdMap() : Collections.emptyList();
    }
    
    private TeBlockRegistry() {
    }
    
    static {
        NAME_REGISTRY = new HashMap<String, ITeBlock>();
        CLASS_REGISTRY = new IdentityHashMap<Class<? extends TileEntityBlock>, ITeBlock>();
        OLD_REGISTRY = new HashMap<String, Class<? extends TileEntityBlock>>();
        RESOURCE_REGISTRY = new HashMap<ResourceLocation, TeBlockInfo<?>>(5);
    }
    
    public static class TeBlockInfo<E extends Enum<E> & ITeBlock>
    {
        private BlockTileEntity block;
        private final boolean specialModels;
        private Material defaultMaterial;
        private ITeBlock.ITeBlockCreativeRegisterer creativeRegisterer;
        private final Set<E> teBlocks;
        private final List<ITeBlock> idMap;
        
        TeBlockInfo(final E universe) {
            this(universe.getClass());
        }
        
        TeBlockInfo(final Class<E> universe) {
            this.defaultMaterial = Material.IRON;
            this.idMap = new ArrayList<ITeBlock>();
            this.teBlocks = EnumSet.noneOf(universe);
            this.specialModels = ITeBlockSpecialItem.class.isAssignableFrom(universe);
        }
        
        void register(final E block) {
            if (!this.teBlocks.add(block)) {
                throw new IllegalStateException("ITeBlock already registered!");
            }
            TeBlockRegistry.addName(block);
            TeBlockRegistry.addClass(block);
            if (block.getId() > -1) {
                final int ID = block.getId();
                while (this.idMap.size() < ID) {
                    this.idMap.add(null);
                }
                if (this.idMap.size() == ID) {
                    this.idMap.add(block);
                }
                else {
                    if (this.idMap.get(ID) != null) {
                        throw new IllegalStateException("The id " + ID + " for " + block + " is already in use by " + this.idMap.get(ID) + '.');
                    }
                    this.idMap.set(ID, block);
                }
            }
        }
        
        void registerAll(final Class<E> universe) {
            for (final E block : EnumSet.allOf(universe)) {
                this.register(block);
            }
        }
        
        void setBlock(final BlockTileEntity block) {
            if (this.hasBlock()) {
                throw new IllegalStateException("Already has block set (" + this.block + ") when adding " + block);
            }
            this.block = block;
        }
        
        public boolean hasBlock() {
            return this.block != null;
        }
        
        public BlockTileEntity getBlock() {
            return this.block;
        }
        
        void setCreativeRegisterer(final ITeBlock.ITeBlockCreativeRegisterer creativeRegisterer) {
            this.creativeRegisterer = creativeRegisterer;
        }
        
        public boolean hasCreativeRegisterer() {
            return this.creativeRegisterer != null;
        }
        
        public ITeBlock.ITeBlockCreativeRegisterer getCreativeRegisterer() {
            return this.creativeRegisterer;
        }
        
        void setDefaultMaterial(final Material material) {
            this.defaultMaterial = material;
        }
        
        public Material getDefaultMaterial() {
            return this.defaultMaterial;
        }
        
        public boolean hasSpecialModels() {
            return this.specialModels;
        }
        
        public Set<? extends ITeBlock> getTeBlocks() {
            return Collections.unmodifiableSet((Set<? extends ITeBlock>)this.teBlocks);
        }
        
        public List<ITeBlock> getIdMap() {
            return Collections.unmodifiableList((List<? extends ITeBlock>)this.idMap);
        }
    }
}
