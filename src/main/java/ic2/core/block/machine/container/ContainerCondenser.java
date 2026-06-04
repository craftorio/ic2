// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityCondenser;

public class ContainerCondenser extends ContainerElectricMachine<TileEntityCondenser>
{
    public ContainerCondenser(final EntityPlayer player, final TileEntityCondenser te) {
        super(player, te, 184, 8, 44);
        this.addSlotToContainer((Slot)new SlotInvSlot(te.waterInputSlot, 0, 26, 73));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.waterOutputSlot, 0, 134, 73));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.upgradeSlot, 0, 152, 73));
        for (int i = 0; i < 2; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(te.ventSlots, i, 26 + i * 108, 26));
            this.addSlotToContainer((Slot)new SlotInvSlot(te.ventSlots, i + 2, 26 + i * 108, 44));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("inputTank");
        ret.add("outputTank");
        ret.add("progress");
        return ret;
    }
}
