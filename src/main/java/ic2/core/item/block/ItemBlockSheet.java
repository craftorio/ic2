// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import ic2.core.block.BlockSheet;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;

public class ItemBlockSheet extends ItemBlockMulti
{
    public ItemBlockSheet(final Block block) {
        super(block);
    }
    
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState newState) {
        return ((BlockSheet)this.block).canReplace(world, pos, side, stack) && super.placeBlockAt(stack, player, world, pos, side, hitX, hitY, hitZ, newState);
    }
}
