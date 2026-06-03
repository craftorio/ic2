package ic2.api.item;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public interface IItemAPI {
  IBlockState getBlockState(String paramString1, String paramString2);
  
  ItemStack getItemStack(String paramString1, String paramString2);
  
  Block getBlock(String paramString);
  
  Item getItem(String paramString);
}
