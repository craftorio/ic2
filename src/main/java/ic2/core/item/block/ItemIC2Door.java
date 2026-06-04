// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.block;

import net.minecraft.item.ItemDoor;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;

public class ItemIC2Door extends ItemBlockIC2
{
    public ItemIC2Door(final Block block) {
        super(block);
        this.setMaxStackSize(8);
    }
    
    public boolean placeBlockAt(final ItemStack stack, final EntityPlayer player, final World world, final BlockPos pos, final EnumFacing side, final float hitX, final float hitY, final float hitZ, final IBlockState newState) {
        ItemDoor.placeDoor(world, pos, EnumFacing.fromAngle((double)player.rotationYaw), this.block, false);
        return true;
    }
}
