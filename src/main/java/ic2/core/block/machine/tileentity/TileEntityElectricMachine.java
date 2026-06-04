// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.comp.Energy;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityElectricMachine extends TileEntityInventory
{
    protected final Energy energy;
    public final InvSlotDischarge dischargeSlot;
    
    public TileEntityElectricMachine(final int maxEnergy, final int tier) {
        this(maxEnergy, tier, true);
    }
    
    public TileEntityElectricMachine(final int maxEnergy, final int tier, final boolean allowRedstone) {
        this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, tier, allowRedstone, InvSlot.InvSide.ANY);
        this.energy = this.addComponent(Energy.asBasicSink(this, maxEnergy, tier).addManagedSlot(this.dischargeSlot));
    }
}
