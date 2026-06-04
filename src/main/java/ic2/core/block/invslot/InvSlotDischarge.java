// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.core.util.StackUtil;
import ic2.api.item.ElectricItem;
import ic2.api.info.Info;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;
import ic2.api.energy.tile.IDischargingSlot;

public class InvSlotDischarge extends InvSlot implements IDischargingSlot
{
    public int tier;
    public boolean allowRedstoneDust;
    
    public InvSlotDischarge(final IInventorySlotHolder<?> base, final Access access, final int tier) {
        this(base, access, tier, InvSide.ANY);
    }
    
    public InvSlotDischarge(final IInventorySlotHolder<?> base, final Access access, final int tier, final InvSide preferredSide) {
        this(base, access, tier, true, preferredSide);
    }
    
    public InvSlotDischarge(final IInventorySlotHolder<?> base, final Access access, final int tier, final boolean allowRedstoneDust, final InvSide preferredSide) {
        super(base, "discharge", access, 1, preferredSide);
        this.allowRedstoneDust = true;
        this.tier = tier;
        this.allowRedstoneDust = allowRedstoneDust;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        return stack != null && (stack.getItem() != Items.REDSTONE || this.allowRedstoneDust) && (Info.itemInfo.getEnergyValue(stack) > 0.0 || ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, this.tier, true, true, true) > 0.0);
    }
    
    @Override
    public double discharge(final double amount, final boolean ignoreLimit) {
        if (amount <= 0.0) {
            throw new IllegalArgumentException("Amount must be > 0.");
        }
        final ItemStack stack = this.get(0);
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        double realAmount = ElectricItem.manager.discharge(stack, amount, this.tier, ignoreLimit, true, false);
        if (realAmount <= 0.0) {
            realAmount = Info.itemInfo.getEnergyValue(stack);
            if (realAmount <= 0.0) {
                return 0.0;
            }
            this.put(0, StackUtil.decSize(stack));
        }
        return realAmount;
    }
    
    public void setTier(final int tier1) {
        this.tier = tier1;
    }
}
