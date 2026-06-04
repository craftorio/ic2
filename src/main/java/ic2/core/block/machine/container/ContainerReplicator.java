// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityReplicator;

public class ContainerReplicator extends ContainerElectricMachine<TileEntityReplicator>
{
    public ContainerReplicator(final EntityPlayer player, final TileEntityReplicator tileEntity1) {
        super(player, tileEntity1, 184, 152, 83);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, 90, 59));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.fluidSlot, 0, 8, 27));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.cellSlot, 0, 8, 72));
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.upgradeSlot, i, 152, 8 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        ret.add("uuProcessed");
        ret.add("pattern");
        ret.add("mode");
        ret.add("index");
        ret.add("maxIndex");
        ret.add("patternUu");
        ret.add("patternEu");
        return ret;
    }
}
