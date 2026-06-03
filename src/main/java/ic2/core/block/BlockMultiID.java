package ic2.core.block;

import ic2.core.block.state.EnumProperty;
import ic2.core.block.state.IIdProvider;
import ic2.core.block.type.IBlockSound;
import ic2.core.block.type.IExtBlockType;
import ic2.core.item.block.ItemBlockMulti;
import ic2.core.ref.BlockName;
import ic2.core.ref.IMultiBlock;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class BlockMultiID<T extends Enum<T> & IIdProvider> extends BlockBase implements IMultiBlock<T> {
  public static <T extends Enum<T> & IIdProvider> BlockMultiID<T> create(BlockName name, Material material, Class<T> typeClass) {
    EnumProperty<T> typeProperty = createTypeProperty(typeClass);
    currentTypeProperty.set(typeProperty);
    BlockMultiID<T> ret = new BlockMultiID<>(name, material);
    currentTypeProperty.remove();
    return ret;
  }
  
  private static <T extends Enum<T> & IIdProvider> EnumProperty<T> createTypeProperty(Class<T> typeClass) {
    EnumProperty<T> ret = new EnumProperty("type", typeClass);
    if (ret.getAllowedValues().size() > 16)
      throw new IllegalArgumentException("Too many values to fit in 16 meta values for " + typeClass); 
    return ret;
  }
  
  protected static <T extends Enum<T> & IIdProvider, U extends BlockMultiID<T>> U create(Class<U> blockClass, Class<T> typeClass, Object... ctorArgs) {
    BlockMultiID blockMultiID;
    EnumProperty<T> typeProperty = createTypeProperty(typeClass);
    Constructor<U> ctor = null;
    label33: for (Constructor<?> cCtor : blockClass.getDeclaredConstructors()) {
      Class<?>[] parameterTypes = cCtor.getParameterTypes();
      if (parameterTypes.length == ctorArgs.length) {
        for (int i = 0; i < parameterTypes.length; ) {
          Class<?> type = parameterTypes[i];
          Object arg = ctorArgs[i];
          if (arg != null || !type.isPrimitive()) {
            if (arg != null && 
              !parameterTypes[i].isInstance(arg))
              continue label33; 
            i++;
          } 
          continue label33;
        } 
        if (ctor != null)
          throw new IllegalArgumentException("ambiguous constructor"); 
        ctor = (Constructor)cCtor;
      } 
    } 
    if (ctor == null)
      throw new IllegalArgumentException("no matching constructor"); 
    currentTypeProperty.set(typeProperty);
    try {
      ctor.setAccessible(true);
      blockMultiID = (BlockMultiID)ctor.newInstance(ctorArgs);
    } catch (Exception e) {
      throw new RuntimeException(e);
    } finally {
      currentTypeProperty.remove();
    } 
    return (U)blockMultiID;
  }
  
  protected BlockMultiID(BlockName name, Material material) {
    this(name, material, (Class)ItemBlockMulti.class);
  }
  
  protected BlockMultiID(BlockName name, Material material, Class<? extends ItemBlock> itemClass) {
    super(name, material, itemClass);
    this.typeProperty = getTypeProperty();
    func_180632_j(this.field_176227_L.func_177621_b()
        .func_177226_a((IProperty)this.typeProperty, this.typeProperty.getDefault()));
  }
  
  @SideOnly(Side.CLIENT)
  public void registerModels(BlockName name) {
    registerItemModels(this, getTypeStates());
  }
  
  protected final List<IBlockState> getTypeStates() {
    List<IBlockState> ret = new ArrayList<>(this.typeProperty.getAllowedValues().size());
    for (Enum enum_ : this.typeProperty.getAllowedValues())
      ret.add(func_176223_P().func_177226_a((IProperty)this.typeProperty, enum_)); 
    return ret;
  }
  
  protected BlockStateContainer func_180661_e() {
    return new BlockStateContainer(this, new IProperty[] { (IProperty)getTypeProperty() });
  }
  
  public IBlockState func_176203_a(int meta) {
    EnumProperty<T> typeProperty = getTypeProperty();
    return func_176223_P().func_177226_a((IProperty)typeProperty, typeProperty.getValueOrDefault(meta));
  }
  
  public int func_176201_c(IBlockState state) {
    return ((IIdProvider)state.func_177229_b((IProperty)getTypeProperty())).getId();
  }
  
  public T getType(IBlockAccess world, BlockPos pos) {
    return getType(world.func_180495_p(pos));
  }
  
  public final T getType(IBlockState state) {
    if (state.func_177230_c() != this)
      return null; 
    return (T)state.func_177229_b((IProperty)this.typeProperty);
  }
  
  public IBlockState getState(T type) {
    if (type == null)
      throw new IllegalArgumentException("invalid type: " + type); 
    return func_176223_P().func_177226_a((IProperty)this.typeProperty, (Comparable)type);
  }
  
  public IBlockState getState(String variant) {
    if (variant == null)
      return func_176223_P(); 
    for (Enum enum_ : this.typeProperty.getAllowedValues()) {
      if (enum_.name().equals(variant))
        return getState((T)enum_); 
    } 
    throw new IllegalArgumentException("Invalid type " + variant + " for " + this);
  }
  
  public ItemStack getItemStack(T type) {
    return getItemStack(getState(type));
  }
  
  public ItemStack getItemStack(String variant) {
    if (variant == null)
      throw new IllegalArgumentException("invalid type: " + variant); 
    Enum enum_ = this.typeProperty.getValue(variant);
    if (enum_ == null)
      throw new IllegalArgumentException("invalid variant " + variant + " for " + this); 
    return getItemStack((T)enum_);
  }
  
  public String getVariant(ItemStack stack) {
    if (stack == null)
      throw new NullPointerException("null stack"); 
    Item item = Item.func_150898_a(this);
    if (stack.func_77973_b() != item)
      throw new IllegalArgumentException("The stack " + stack + " doesn't match " + item + " (" + this + ")"); 
    IBlockState state = func_176203_a(stack.func_77960_j());
    T type = getType(state);
    return ((IIdProvider)type).getName();
  }
  
  public ItemStack getItemStack(IBlockState state) {
    if (state.func_177230_c() != this)
      return null; 
    Item item = Item.func_150898_a(this);
    if (item == null || item == Items.field_190931_a)
      throw new RuntimeException("no matching item for " + this); 
    int meta = func_176201_c(state);
    return new ItemStack(item, 1, meta);
  }
  
  public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
    ItemStack stack = getItemStack(state);
    if (stack == null)
      return new ArrayList<>(); 
    List<ItemStack> ret = new ArrayList<>();
    ret.add(stack);
    return ret;
  }
  
  public void func_149666_a(CreativeTabs tabs, NonNullList<ItemStack> itemList) {
    for (Enum enum_ : this.typeProperty.getShownValues())
      itemList.add(getItemStack((T)enum_)); 
  }
  
  public Set<T> getAllTypes() {
    return EnumSet.allOf(this.typeProperty.func_177699_b());
  }
  
  public ItemStack func_185473_a(World world, BlockPos pos, IBlockState state) {
    return getItemStack(state);
  }
  
  public final EnumProperty<T> getTypeProperty() {
    EnumProperty<T> ret;
    if (this.typeProperty != null) {
      ret = this.typeProperty;
    } else {
      ret = (EnumProperty<T>)currentTypeProperty.get();
      if (ret == null)
        throw new IllegalStateException("The type property can't be obtained."); 
    } 
    return ret;
  }
  
  public float func_176195_g(IBlockState state, World world, BlockPos pos) {
    if (IExtBlockType.class.isAssignableFrom(this.typeProperty.func_177699_b())) {
      T type = getType(state);
      if (type != null)
        return ((IExtBlockType)type).getHardness(); 
    } 
    return super.func_176195_g(state, world, pos);
  }
  
  public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
    if (IExtBlockType.class.isAssignableFrom(this.typeProperty.func_177699_b())) {
      T type = getType((IBlockAccess)world, pos);
      if (type != null)
        return ((IExtBlockType)type).getExplosionResistance(); 
    } 
    return super.getExplosionResistance(world, pos, exploder, explosion);
  }
  
  public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
    if (IBlockSound.class.isAssignableFrom(this.typeProperty.func_177699_b())) {
      T type = getType(state);
      if (type != null)
        return ((IBlockSound)type).getSound(); 
    } 
    return super.getSoundType(state, world, pos, entity);
  }
  
  private static final ThreadLocal<EnumProperty<? extends Enum<?>>> currentTypeProperty = new UnstartingThreadLocal<>();
  
  protected final EnumProperty<T> typeProperty;
}
