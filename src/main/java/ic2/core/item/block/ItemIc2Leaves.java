package ic2.core.item.block;

import ic2.core.block.Ic2Leaves;
import ic2.core.init.Localization;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLeaves;
import net.minecraft.item.ItemLeaves;
import net.minecraft.item.ItemStack;

public class ItemIc2Leaves extends ItemLeaves {
   public ItemIc2Leaves(Block block) {
      super((BlockLeaves)block);
      this.setHasSubtypes(false);
   }

   public String getUnlocalizedName() {
      return "ic2." + super.getUnlocalizedName().substring(5);
   }

   public String getUnlocalizedName(ItemStack stack) {
      return this.getUnlocalizedName()
         + "."
         + ((Ic2Leaves.LeavesType)this.block.getStateFromMeta(stack.getMetadata()).getValue(Ic2Leaves.typeProperty)).getName();
   }

   public String getItemStackDisplayName(ItemStack stack) {
      return Localization.translate(this.getUnlocalizedName(stack));
   }
}
