// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlotReadOnly;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerTradeOMatClosed extends ContainerFullInv<TileEntityTradeOMat>
{
    public ContainerTradeOMatClosed(final EntityPlayer player, final TileEntityTradeOMat tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 19));
        this.addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.offerSlot, 0, 50, 38));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.inputSlot, 0, 143, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, 143, 53));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("stock");
        return ret;
    }
}
