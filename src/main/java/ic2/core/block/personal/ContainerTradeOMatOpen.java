// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerTradeOMatOpen extends ContainerFullInv<TileEntityTradeOMat>
{
    public ContainerTradeOMatOpen(final EntityPlayer player, final TileEntityTradeOMat tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.demandSlot, 0, 50, 19));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.offerSlot, 0, 50, 53));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.inputSlot, 0, 80, 19));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, 80, 53));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("stock");
        ret.add("totalTradeCount");
        return ret;
    }
}
