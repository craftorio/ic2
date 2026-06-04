// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.List;
import net.minecraft.util.EnumFacing;
import net.minecraft.inventory.IInventory;

public interface IWeightedDistributor extends IInventory
{
    EnumFacing getFacing();
    
    @SideOnly(Side.CLIENT)
    List<EnumFacing> getPriority();
    
    void updatePriority(final boolean p0);
}
