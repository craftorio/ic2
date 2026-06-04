// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityCropmatron;

public class ContainerCropmatron extends ContainerElectricMachine<TileEntityCropmatron>
{
    public ContainerCropmatron(final EntityPlayer player, final TileEntityCropmatron base) {
        super(player, base, 192, 134, 80);
        for (int i = 0; i < base.fertilizerSlot.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.fertilizerSlot, i, 8 + i * 18, 80));
        }
        this.addSlotToContainer((Slot)new SlotInvSlot(base.exInputSlot, 0, 49, 27));
        this.addSlotToContainer((Slot)new SlotInvSlot(base.exOutputSlot, 0, 67, 27));
        this.addSlotToContainer((Slot)new SlotInvSlot(base.wasserinputSlot, 0, 57, 56));
        this.addSlotToContainer((Slot)new SlotInvSlot(base.wasseroutputSlot, 0, 75, 56));
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.upgradeSlot, i, 152, 26 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("waterTank");
        ret.add("exTank");
        return ret;
    }
}
