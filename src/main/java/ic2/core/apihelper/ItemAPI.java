package ic2.core.apihelper;

import ic2.api.item.IItemAPI;
import ic2.core.ref.BlockName;
import ic2.core.ref.FluidName;
import ic2.core.ref.IMultiBlock;
import ic2.core.ref.ItemName;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemAPI implements IItemAPI {
   @Override
   public ItemStack getItemStack(String name, String variant) {
      if (name == null) {
         return null;
      }

      if (variant == null) {
         int idx = name.indexOf(35);
         if (idx != -1) {
            variant = name.substring(idx + 1);
            name = name.substring(0, idx);
         }
      }

      BlockName blockName = this.getBlockName(name);
      if (blockName != null) {
         return blockName.getItemStack(variant);
      }

      ItemName itemName = this.getItemName(name);
      return itemName != null ? itemName.getItemStack(variant) : null;
   }

   @Override
   public Block getBlock(String name) {
      if (name == null) {
         return null;
      }

      BlockName blockName = this.getBlockName(name);
      if (blockName != null) {
         return blockName.getInstance();
      }

      FluidName fluidName = this.getFluidName(name);
      return fluidName != null ? fluidName.getInstance().getBlock() : null;
   }

   @Override
   public Item getItem(String name) {
      if (name == null) {
         return null;
      }

      ItemName itemName = this.getItemName(name);
      if (itemName != null) {
         return itemName.getInstance();
      }

      Block block = this.getBlock(name);
      if (block != null) {
         Item ret = Item.getItemFromBlock(block);
         if (ret != Items.AIR || block == Blocks.AIR) {
            return ret;
         }
      }

      return null;
   }

   @Override
   public IBlockState getBlockState(String name, String variant) {
      if (variant == null) {
         int idx = name.indexOf(35);
         if (idx != -1) {
            variant = name.substring(idx + 1);
            name = name.substring(0, idx);
         }
      }

      BlockName blockName = this.getBlockName(name);
      if (blockName != null) {
         Block block = blockName.getInstance();
         if (block instanceof IMultiBlock) {
            return ((IMultiBlock)block).getState(variant);
         }

         assert variant == null;
         return block.getDefaultState();
      } else {
         FluidName fluidName = this.getFluidName(name);
         if (fluidName != null) {
            assert variant == null;
            return fluidName.getInstance().getBlock().getDefaultState();
         } else {
            return null;
         }
      }
   }

   private ItemName getItemName(String itemName) {
      for (ItemName name : ItemName.values) {
         if (name.name().equalsIgnoreCase(itemName)) {
            return name;
         }
      }

      return null;
   }

   private BlockName getBlockName(String blockName) {
      for (BlockName name : BlockName.values) {
         if (name.name().equalsIgnoreCase(blockName)) {
            return name;
         }
      }

      return null;
   }

   private FluidName getFluidName(String fluidName) {
      for (FluidName name : FluidName.values) {
         if (name.name().equalsIgnoreCase(fluidName)) {
            return name;
         }
      }

      return null;
   }
}
