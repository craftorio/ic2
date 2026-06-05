package ic2.core.item;

import ic2.api.recipe.Recipes;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.BlockDispenser;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IPosition;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;

public class BehaviorScrapboxDispense extends BehaviorDefaultDispenseItem {
   protected ItemStack dispenseStack(IBlockSource blockSource, ItemStack stack) {
      if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
         EnumFacing facing = (EnumFacing)blockSource.getBlockState().getValue(BlockDispenser.FACING);
         IPosition position = BlockDispenser.getDispensePosition(blockSource);
         doDispense(blockSource.getWorld(), Recipes.scrapboxDrops.getDrop(stack, true), 6, facing, position);
      }

      return stack;
   }
}
