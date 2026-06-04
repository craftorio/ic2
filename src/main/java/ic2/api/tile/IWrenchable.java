// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.tile;

import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IWrenchable
{
    EnumFacing getFacing(final World p0, final BlockPos p1);
    
    default boolean canSetFacing(final World world, final BlockPos pos, final EnumFacing newDirection, final EntityPlayer player) {
        return true;
    }
    
    boolean setFacing(final World p0, final BlockPos p1, final EnumFacing p2, final EntityPlayer p3);
    
    boolean wrenchCanRemove(final World p0, final BlockPos p1, final EntityPlayer p2);
    
    List<ItemStack> getWrenchDrops(final World p0, final BlockPos p1, final IBlockState p2, final TileEntity p3, final EntityPlayer p4, final int p5);
}
