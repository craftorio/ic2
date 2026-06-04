// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.core.util.StackUtil;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.energy.tile.IChargingSlot;

public class InvSlotCharge extends InvSlot implements IChargingSlot
{
    public int tier;
    
    public InvSlotCharge(final IInventorySlotHolder<?> base1, final int tier) {
        super(base1, "charge", Access.IO, 1, InvSide.TOP);
        this.tier = tier;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, this.tier, true, true) > 0.0;
    }
    
    @Override
    public double charge(final double amount) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        final ItemStack stack = this.get(0);
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        return ElectricItem.manager.charge(stack, amount, this.tier, false, false);
    }
    
    public void setTier(final int tier1) {
        this.tier = tier1;
    }
}
