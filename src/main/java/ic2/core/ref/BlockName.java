package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public enum BlockName {
   te,
   resource,
   leaves,
   rubber_wood,
   sapling,
   scaffold,
   foam,
   fence,
   sheet,
   glass,
   wall,
   mining_pipe,
   reinforced_door,
   dynamite,
   refractory_bricks;

   private Block instance;
   public static final BlockName[] values = values();

   public boolean hasInstance() {
      return this.instance != null;
   }

   public <T extends Block & IBlockModelProvider> T getInstance() {
      if (this.instance == null) {
         throw new IllegalStateException("the requested block instance for " + this.name() + " isn't set (yet)");
      } else {
         return (T)this.instance;
      }
   }

   public <T extends Block & IBlockModelProvider> void setInstance(T instance) {
      if (this.instance != null) {
         throw new IllegalStateException("conflicting instance");
      }

      this.instance = instance;
   }

   public <T extends IIdProvider> IBlockState getBlockState(T variant) {
      if (this.instance == null) {
         return null;
      } else if (this.instance instanceof IMultiBlock) {
         IMultiBlock<T> block = (IMultiBlock<T>)this.instance;
         return block.getState(variant);
      } else if (variant == null) {
         return this.instance.getDefaultState();
      } else {
         throw new IllegalArgumentException("not applicable");
      }
   }

   public boolean hasItemStack() {
      if (this.instance == null) {
         return false;
      }

      if (this.instance instanceof IMultiItem) {
         return true;
      }

      Item item = Item.getItemFromBlock(this.instance);
      return item != null && item != Items.AIR;
   }

   public <T extends Enum<T> & IIdProvider> ItemStack getItemStack() {
      return this.getItemStack((String)null);
   }

   public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(T variant) {
      if (this.instance == null) {
         return null;
      } else if (this.instance instanceof IMultiItem) {
         IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
         return multiItem.getItemStack(variant);
      } else if (variant == null) {
         return this.getItemStack((String)null);
      } else {
         throw new IllegalArgumentException("not applicable");
      }
   }

   public <T extends Enum<T> & IIdProvider> ItemStack getItemStack(String variant) {
      if (this.instance == null) {
         return null;
      }

      if (this.instance instanceof IMultiItem) {
         IMultiItem<T> multiItem = (IMultiItem<T>)this.instance;
         return multiItem.getItemStack(variant);
      }

      if (variant == null) {
         Item item = Item.getItemFromBlock(this.instance);
         if (item != null && item != Items.AIR) {
            return new ItemStack(item);
         } else {
            throw new IllegalArgumentException("No item found for " + this.instance);
         }
      } else {
         throw new IllegalArgumentException("not applicable");
      }
   }

   public String getVariant(ItemStack stack) {
      if (this.instance == null) {
         return null;
      } else {
         return this.instance instanceof IMultiItem ? ((IMultiItem)this.instance).getVariant(stack) : null;
      }
   }
}
