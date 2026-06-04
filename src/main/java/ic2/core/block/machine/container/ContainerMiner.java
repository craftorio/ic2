// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityMiner;

public class ContainerMiner extends ContainerElectricMachine<TileEntityMiner>
{
    public ContainerMiner(final EntityPlayer player, final TileEntityMiner tileEntity1) {
        super(player, tileEntity1, 166, 152, 58);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.scannerSlot, 0, 8, 58));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.pipeSlot, 0, 8, 40));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.drillSlot, 0, 8, 22));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.upgradeSlot, 0, 152, 22));
        for (int y = 0; y < tileEntity1.buffer.size() / 5; ++y) {
            for (int x = 0; x < 5; ++x) {
                this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.buffer, x + y * 5, 44 + x * 18, 22 + y * 18));
            }
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("progress");
        return ret;
    }
}
