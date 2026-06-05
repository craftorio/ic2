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
   private static final ThreadLocal<EnumProperty<? extends Enum<?>>> currentTypeProperty = new UnstartingThreadLocal<>();
   protected final EnumProperty<T> typeProperty = this.getTypeProperty();

   public static <T extends Enum<T> & IIdProvider> BlockMultiID<T> create(BlockName name, Material material, Class<T> typeClass) {
      EnumProperty<T> typeProperty = createTypeProperty(typeClass);
      currentTypeProperty.set(typeProperty);
      BlockMultiID<T> ret = new BlockMultiID<>(name, material);
      currentTypeProperty.remove();
      return ret;
   }

   private static <T extends Enum<T> & IIdProvider> EnumProperty<T> createTypeProperty(Class<T> typeClass) {
      EnumProperty<T> ret = new EnumProperty<>("type", typeClass);
      if (ret.getAllowedValues().size() > 16) {
         throw new IllegalArgumentException("Too many values to fit in 16 meta values for " + typeClass);
      } else {
         return ret;
      }
   }

   protected static <T extends Enum<T> & IIdProvider, U extends BlockMultiID<T>> U create(Class<U> blockClass, Class<T> typeClass, Object... ctorArgs) {
      EnumProperty<T> typeProperty = createTypeProperty(typeClass);
      Constructor<U> ctor = null;

      label90:
      for (Constructor<?> cCtor : blockClass.getDeclaredConstructors()) {
         Class<?>[] parameterTypes = cCtor.getParameterTypes();
         if (parameterTypes.length == ctorArgs.length) {
            for (int i = 0; i < parameterTypes.length; i++) {
               Class<?> type = parameterTypes[i];
               Object arg = ctorArgs[i];
               if (arg == null && type.isPrimitive() || arg != null && !parameterTypes[i].isInstance(arg)) {
                  continue label90;
               }
            }

            if (ctor != null) {
               throw new IllegalArgumentException("ambiguous constructor");
            }

            ctor = (Constructor<U>)cCtor;
         }
      }

      if (ctor == null) {
         throw new IllegalArgumentException("no matching constructor");
      }

      currentTypeProperty.set(typeProperty);

      U ret;
      try {
         ctor.setAccessible(true);
         ret = (U)ctor.newInstance(ctorArgs);
      } catch (Exception e) {
         throw new RuntimeException(e);
      } finally {
         currentTypeProperty.remove();
      }

      return ret;
   }

   protected BlockMultiID(BlockName name, Material material) {
      this(name, material, ItemBlockMulti.class);
   }

   protected BlockMultiID(BlockName name, Material material, Class<? extends ItemBlock> itemClass) {
      super(name, material, itemClass);
      this.setDefaultState(this.blockState.getBaseState().withProperty(this.typeProperty, this.typeProperty.getDefault()));
   }

   @SideOnly(Side.CLIENT)
   @Override
   public void registerModels(BlockName name) {
      registerItemModels(this, this.getTypeStates());
   }

   protected final List<IBlockState> getTypeStates() {
      List<IBlockState> ret = new ArrayList<>(this.typeProperty.getAllowedValues().size());

      for (T type : this.typeProperty.getAllowedValues()) {
         ret.add(this.getDefaultState().withProperty(this.typeProperty, type));
      }

      return ret;
   }

   protected BlockStateContainer createBlockState() {
      return new BlockStateContainer(this, new IProperty[]{this.getTypeProperty()});
   }

   public IBlockState getStateFromMeta(int meta) {
      EnumProperty<T> typeProperty = this.getTypeProperty();
      return this.getDefaultState().withProperty(typeProperty, typeProperty.getValueOrDefault(meta));
   }

   public int getMetaFromState(IBlockState state) {
      return ((IIdProvider)((Enum)state.getValue(this.getTypeProperty()))).getId();
   }

   public T getType(IBlockAccess world, BlockPos pos) {
      return this.getType(world.getBlockState(pos));
   }

   public final T getType(IBlockState state) {
      return (T)(state.getBlock() != this ? null : state.getValue(this.typeProperty));
   }

   public IBlockState getState(T type) {
      if (type == null) {
         throw new IllegalArgumentException("invalid type: " + type);
      } else {
         return this.getDefaultState().withProperty(this.typeProperty, type);
      }
   }

   @Override
   public IBlockState getState(String variant) {
      if (variant == null) {
         return this.getDefaultState();
      }

      for (T type : this.typeProperty.getAllowedValues()) {
         if (type.name().equals(variant)) {
            return this.getState(type);
         }
      }

      throw new IllegalArgumentException("Invalid type " + variant + " for " + this);
   }

   public ItemStack getItemStack(T type) {
      return this.getItemStack(this.getState(type));
   }

   @Override
   public ItemStack getItemStack(String variant) {
      if (variant == null) {
         throw new IllegalArgumentException("invalid type: " + variant);
      } else {
         T type = this.typeProperty.getValue(variant);
         if (type == null) {
            throw new IllegalArgumentException("invalid variant " + variant + " for " + this);
         } else {
            return this.getItemStack(type);
         }
      }
   }

   @Override
   public String getVariant(ItemStack stack) {
      if (stack == null) {
         throw new NullPointerException("null stack");
      }

      Item item = Item.getItemFromBlock(this);
      if (stack.getItem() != item) {
         throw new IllegalArgumentException("The stack " + stack + " doesn't match " + item + " (" + this + ")");
      }

      IBlockState state = this.getStateFromMeta(stack.getMetadata());
      T type = this.getType(state);
      return type.getName();
   }

   public ItemStack getItemStack(IBlockState state) {
      if (state.getBlock() != this) {
         return null;
      } else {
         Item item = Item.getItemFromBlock(this);
         if (item != null && item != Items.AIR) {
            int meta = this.getMetaFromState(state);
            return new ItemStack(item, 1, meta);
         } else {
            throw new RuntimeException("no matching item for " + this);
         }
      }
   }

   public List<ItemStack> getDrops(IBlockAccess world, BlockPos pos, IBlockState state, int fortune) {
      ItemStack stack = this.getItemStack(state);
      if (stack == null) {
         return new ArrayList<>();
      }

      List<ItemStack> ret = new ArrayList<>();
      ret.add(stack);
      return ret;
   }

   public void getSubBlocks(CreativeTabs tabs, NonNullList<ItemStack> itemList) {
      for (T type : this.typeProperty.getShownValues()) {
         itemList.add(this.getItemStack(type));
      }
   }

   @Override
   public Set<T> getAllTypes() {
      return EnumSet.allOf(this.typeProperty.getValueClass());
   }

   public ItemStack getItem(World world, BlockPos pos, IBlockState state) {
      return this.getItemStack(state);
   }

   public final EnumProperty<T> getTypeProperty() {
      EnumProperty<T> ret;
      if (this.typeProperty != null) {
         ret = this.typeProperty;
      } else {
         ret = (EnumProperty<T>)currentTypeProperty.get();
         if (ret == null) {
            throw new IllegalStateException("The type property can't be obtained.");
         }
      }

      return ret;
   }

   public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
      if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass())) {
         T type = this.getType(state);
         if (type != null) {
            return ((IExtBlockType)type).getHardness();
         }
      }

      return super.getBlockHardness(state, world, pos);
   }

   public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
      if (IExtBlockType.class.isAssignableFrom(this.typeProperty.getValueClass())) {
         T type = this.getType(world, pos);
         if (type != null) {
            return ((IExtBlockType)type).getExplosionResistance();
         }
      }

      return super.getExplosionResistance(world, pos, exploder, explosion);
   }

   public SoundType getSoundType(IBlockState state, World world, BlockPos pos, Entity entity) {
      if (IBlockSound.class.isAssignableFrom(this.typeProperty.getValueClass())) {
         T type = this.getType(state);
         if (type != null) {
            return ((IBlockSound)type).getSound();
         }
      }

      return super.getSoundType(state, world, pos, entity);
   }
}
