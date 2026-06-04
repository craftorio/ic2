// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.dispenser.IPosition;
import ic2.api.recipe.Recipes;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.BlockDispenser;
import net.minecraft.util.EnumFacing;
import ic2.core.util.StackUtil;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;

public class BehaviorScrapboxDispense extends BehaviorDefaultDispenseItem
{
    protected ItemStack dispenseStack(final IBlockSource blockSource, final ItemStack stack) {
        if (StackUtil.checkItemEquality(stack, ItemName.crafting.getItemStack(CraftingItemType.scrap_box))) {
            final EnumFacing facing = (EnumFacing)blockSource.getBlockState().getValue((IProperty)BlockDispenser.FACING);
            final IPosition position = BlockDispenser.getDispensePosition(blockSource);
            doDispense(blockSource.getWorld(), Recipes.scrapboxDrops.getDrop(stack, true), 6, facing, position);
        }
        return stack;
    }
}
