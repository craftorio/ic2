// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.block.type.IBlockSound;
import net.minecraft.block.SoundType;
import net.minecraft.world.Explosion;
import net.minecraft.entity.Entity;
import ic2.core.block.type.IExtBlockType;
import net.minecraft.world.World;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.block.state.BlockStateContainer;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.block.state.IBlockState;
import net.minecraft.block.Block;
import net.minecraft.block.properties.IProperty;
import net.minecraft.item.ItemBlock;
import ic2.core.item.block.ItemBlockMulti;
import java.lang.reflect.Constructor;
import net.minecraft.block.material.Material;
import ic2.core.ref.BlockName;
import ic2.core.block.state.EnumProperty;
import ic2.core.ref.IMultiBlock;
import ic2.core.block.state.IIdProvider;

public class BlockMultiID<T extends Enum<T> & IIdProvider> extends BlockBase implements IMultiBlock<T>
{
    private static final ThreadLocal<EnumProperty<? extends Enum<?>>> currentTypeProperty;
    protected final EnumProperty<T> typeProperty;
    
    public static <T extends Enum<T> & IIdProvider> BlockMultiID<T> create(final BlockName name, final Material material, final Class<T> typeClass) {
        final EnumProperty<T> typeProperty = createTypeProperty(typeClass);
        BlockMultiID.currentTypeProperty.set(typeProperty);
        final BlockMultiID<T> ret = new BlockMultiID<T>(name, material);
        BlockMultiID.currentTypeProperty.remove();
        return ret;
    }
    
    private static <T extends Enum<T> & IIdProvider> EnumProperty<T> createTypeProperty(final Class<T> typeClass) {
        final EnumProperty<T> ret = new EnumProperty<T>("type", typeClass);
        if (ret.getAllowedValues().size() > 16) {
            throw new IllegalArgumentException("Too many values to fit in 16 meta values for " + typeClass);
        }
        return ret;
    }
    
    protected static <T extends Enum<T> & IIdProvider, U extends BlockMultiID<T>> U create(final Class<U> blockClass, final Class<T> typeClass, final Object... ctorArgs) {
        final EnumProperty<T> typeProperty = createTypeProperty(typeClass);
        Constructor<U> ctor = null;
        for (final Constructor<?> cCtor : blockClass.getDeclaredConstructors()) {
            final Class<?>[] parameterTypes = cCtor.getParameterTypes();
            Label_0137: {
                if (parameterTypes.length == ctorArgs.length) {
                    for (int i = 0; i < parameterTypes.length; ++i) {
                        final Class<?> type = parameterTypes[i];
                        final Object arg = ctorArgs[i];
                        if (arg == null && type.isPrimitive()) {
                            break Label_0137;
                        }
                        if (arg != null && !parameterTypes[i].isInstance(arg)) {
                            break Label_0137;
                        }
                    }
                    if (ctor != null) {
                        throw new IllegalArgumentException("ambiguous constructor");
                    }
                    ctor = (Constructor<U>)cCtor;
                }
            }
        }
        if (ctor == null) {
            throw new IllegalArgumentException("no matching constructor");
        }
        BlockMultiID.currentTypeProperty.set(typeProperty);
        U ret;
        try {
            ctor.setAccessible(true);
            ret = ctor.newInstance(ctorArgs);
        }
        catch (final Exception e) {
            throw new RuntimeException(e);
        }
        finally {
            BlockMultiID.currentTypeProperty.remove();
        }
        return ret;
    }
    
    protected BlockMultiID(final BlockName name, final Material material) {
        this(name, material, ItemBlockMulti.class);
    }
    
    protected BlockMultiID(final BlockName name, final Material material, final Class<? extends ItemBlock> itemClass) {
        super(name, material, itemClass);
        this.typeProperty = this.getTypeProperty();
        this.setDefaultState(this.blockState.getBaseState().withProperty((IProperty)this.typeProperty, (Comparable)this.typeProperty.getDefault()));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public void registerModels(final BlockName name) {
        BlockBase.registerItemModels(this, this.getTypeStates());
    }
    
    protected final List<IBlockState> getTypeStates() {
        final List<IBlockState> ret = new ArrayList<IBlockState>(this.typeProperty.getAllowedValues().size());
        for (final T type : this.typeProperty.getAllowedValues()) {
            ret.add(this.getDefaultState().withProperty((IProperty)this.typeProperty, (Comparable)type));
        }
        return ret;
    }
    
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer((Block)this, new IProperty[] { (IProperty)this.getTypeProperty() });
    }
    
    public IBlockState getStateFromMeta(final int meta) {
        final EnumProperty<T> typeProperty = this.getTypeProperty();
        return this.getDefaultState().withProperty((IProperty)typeProperty, (Comparable)typeProperty.getValueOrDefault(meta));
    }
    
    public int getMetaFromState(final IBlockState state) {
        return ((IIdProvider)state.getValue((IProperty)this.getTypeProperty())).getId();
    }
    
    public T getType(final IBlockAccess world, final BlockPos pos) {
        return this.getType(world.getBlockState(pos));
    }
    
    public final T getType(final IBlockState state) {
        if (state.getBlock() != this) {
            return null;
        }
        return (T)state.getValue((IProperty)this.typeProperty);
    }
    
    @Override
    public IBlockState getState(final T type) {
        if (type == null) {
            throw new IllegalArgumentException("invalid type: " + type);
        }
        return this.getDefaultState().withProperty((IProperty)this.typeProperty, (Comparable)type);
    }
    
    @Override
    public IBlockState getState(final String variant) {
        if (variant == null) {
            return this.getDefaultState();
        }
        for (final T type : this.typeProperty.getAllowedValues()) {
            if (type.name().equals(variant)) {
                return this.getState(type);
            }
        }
        throw new IllegalArgumentException("Invalid type " + variant + " for " + this);
    }
    
    public ItemStack getItemStack(final T type) {
        return this.getItemStack(this.getState(type));
    }
    
    public ItemStack getItemStack(final String variant) {
        if (variant == null) {
            throw new IllegalArgumentException("invalid type: " + variant);
        }
        final T type = this.typeProperty.getValue(variant);
        if (type == null) {
            throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
        }
        return this.getItemStack(type);
    }
    
    public String getVariant(final ItemStack stack) {
        if (stack == null) {
            throw new NullPointerException("null stack");
        }
        final Item item = Item.getItemFromBlock((Block)this);
        if (stack.getItem() != item) {
            throw new IllegalArgumentException("The stack " + stack + " doesn't match " + item + " (" + this + ")");
        }
        final IBlockState state = this.getStateFromMeta(stack.getMetadata());
        final T type = this.getType(state);
        return type.getName();
    }
    
    public ItemStack getItemStack(final IBlockState state) {
        if (state.getBlock() != this) {
            return null;
        }
        final Item item = Item.getItemFromBlock((Block)this);
        if (item == null || item == Items.AIR) {
            throw new RuntimeException("no matching item for " + this);
        }
        final int meta = this.getMetaFromState(state);
        return new ItemStack(item, 1, meta);
    }
    
    public List<ItemStack> getDrops(final IBlockAccess world, final BlockPos pos, final IBlockState state, final int fortune) {
        final ItemStack stack = this.getItemStack(state);
        if (stack == null) {
            return new ArrayList<ItemStack>();
        }
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        ret.add(stack);
        return ret;
    }
    
    public void getSubBlocks(final CreativeTabs tabs, final NonNullList<ItemStack> itemList) {
        for (final T type : this.typeProperty.getShownValues()) {
            itemList.add((Object)this.getItemStack(type));
        }
    }
    
    public Set<T> getAllTypes() {
        return (Set<T>)EnumSet.allOf((Class<Enum>)this.typeProperty.getValueClass());
    }
    
    public ItemStack getItem(final World world, final BlockPos pos, final IBlockState state) {
        return this.getItemStack(state);
    }
    
    public final EnumProperty<T> getTypeProperty() {
        EnumProperty<T> ret;
        if (this.typeProperty != null) {
            ret = this.typeProperty;
        }
        else {
            ret = (EnumProperty)BlockMultiID.currentTypeProperty.get();
            if (ret == null) {
                throw new IllegalStateException("The type property can't be obtained.");
            }
        }
        return ret;
    }
    
    public float getBlockHardness(final IBlockState state, final World world, final BlockPos pos) {
        if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass())) {
            final T type = this.getType(state);
            if (type != null) {
                return ((IExtBlockType)type).getHardness();
            }
        }
        return super.getBlockHardness(state, world, pos);
    }
    
    public float getExplosionResistance(final World world, final BlockPos pos, final Entity exploder, final Explosion explosion) {
        if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass())) {
            final T type = this.getType((IBlockAccess)world, pos);
            if (type != null) {
                return ((IExtBlockType)type).getExplosionResistance();
            }
        }
        return super.getExplosionResistance(world, pos, exploder, explosion);
    }
    
    public SoundType getSoundType(final IBlockState state, final World world, final BlockPos pos, final Entity entity) {
        if (IBlockSound.class.isAssignableFrom(this.typeProperty.getValueClass())) {
            final T type = this.getType(state);
            if (type != null) {
                return ((IBlockSound)type).getSound();
            }
        }
        return super.getSoundType(state, world, pos, entity);
    }
    
    static {
        currentTypeProperty = new UnstartingThreadLocal<EnumProperty<? extends Enum<?>>>();
    }
}
