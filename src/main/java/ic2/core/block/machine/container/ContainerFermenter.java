// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.ContainerFullInv;

public class ContainerFermenter extends ContainerFullInv<TileEntityFermenter>
{
    public ContainerFermenter(final EntityPlayer player, final TileEntityFermenter te) {
        super(player, (IInventory)te, 184);
        this.addSlotToContainer((Slot)new SlotInvSlot(te.fluidInputCellInSlot, 0, 14, 46));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.fluidInputCellOutSlot, 0, 14, 64));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.fluidOutputCellInSlot, 0, 148, 43));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.fluidOutputCellOutSlot, 0, 148, 61));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.fertiliserSlot, 0, 86, 83));
        for (int i = 0; i < 2; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(te.upgradeSlot, i, 125 + i * 18, 83));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("inputTank");
        ret.add("outputTank");
        ret.add("progress");
        ret.add("heatBuffer");
        return ret;
    }
}
