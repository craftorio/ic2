// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.item.Item;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.block.state.IBlockState;

public interface IItemAPI
{
    IBlockState getBlockState(final String p0, final String p1);
    
    ItemStack getItemStack(final String p0, final String p1);
    
    Block getBlock(final String p0);
    
    Item getItem(final String p0);
}
