package ic2.api.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IItemAPI {
   IBlockState getBlockState(String var1, String var2);

   ItemStack getItemStack(String var1, String var2);

   Block getBlock(String var1);

   Item getItem(String var1);
}
