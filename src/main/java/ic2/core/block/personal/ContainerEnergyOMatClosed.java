// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.Iterator;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import java.util.List;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlotReadOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerEnergyOMatClosed extends ContainerFullInv<TileEntityEnergyOMat>
{
    private int lastTier;
    
    public ContainerEnergyOMatClosed(final EntityPlayer player, final TileEntityEnergyOMat tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.lastTier = -1;
        this.addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.inputSlot, 0, 143, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.chargeSlot, 0, 143, 53));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("paidFor");
        ret.add("euOffer");
        return ret;
    }
    
    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (final IContainerListener listener : this.listeners) {
            if (((TileEntityEnergyOMat)this.base).chargeSlot.tier != this.lastTier) {
                listener.sendWindowProperty((Container)this, 0, ((TileEntityEnergyOMat)this.base).chargeSlot.tier);
            }
        }
        this.lastTier = ((TileEntityEnergyOMat)this.base).chargeSlot.tier;
    }
    
    public void updateProgressBar(final int index, final int value) {
        super.updateProgressBar(index, value);
        switch (index) {
            case 0: {
                ((TileEntityEnergyOMat)this.base).chargeSlot.tier = value;
                break;
            }
        }
    }
}
