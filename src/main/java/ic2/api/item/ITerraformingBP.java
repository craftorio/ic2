// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface ITerraformingBP
{
    double getConsume(final ItemStack p0);
    
    int getRange(final ItemStack p0);
    
    boolean canInsert(final ItemStack p0, final EntityPlayer p1, final World p2, final BlockPos p3);
    
    boolean terraform(final ItemStack p0, final World p1, final BlockPos p2);
}
