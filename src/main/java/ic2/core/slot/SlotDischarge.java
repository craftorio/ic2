// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import ic2.api.item.ElectricItem;
import ic2.api.info.Info;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class SlotDischarge extends Slot
{
    public int tier;
    
    public SlotDischarge(final IInventory par1iInventory, final int tier1, final int par2, final int par3, final int par4) {
        super(par1iInventory, par2, par3, par4);
        this.tier = Integer.MAX_VALUE;
        this.tier = tier1;
    }
    
    public SlotDischarge(final IInventory par1iInventory, final int par2, final int par3, final int par4) {
        super(par1iInventory, par2, par3, par4);
        this.tier = Integer.MAX_VALUE;
    }
    
    public boolean isItemValid(final ItemStack stack) {
        return stack != null && (Info.itemInfo.getEnergyValue(stack) > 0.0 || ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, this.tier, true, true, true) > 0.0);
    }
}
