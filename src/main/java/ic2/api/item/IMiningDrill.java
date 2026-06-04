// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.item.ItemStack;

public interface IMiningDrill
{
    int energyUse(final ItemStack p0, final World p1, final BlockPos p2, final IBlockState p3);
    
    int breakTime(final ItemStack p0, final World p1, final BlockPos p2, final IBlockState p3);
    
    boolean breakBlock(final ItemStack p0, final World p1, final BlockPos p2, final IBlockState p3);
    
    default boolean tryUsePower(final ItemStack drill, final double amount) {
        return ElectricItem.manager.use(drill, amount, null);
    }
}
