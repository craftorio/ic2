// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityClassicCanner;

public class ContainerClassicCanner extends ContainerElectricMachine<TileEntityClassicCanner>
{
    public ContainerClassicCanner(final EntityPlayer player, final TileEntityClassicCanner base) {
        super(player, base, 166, 30, 45);
        this.addSlotToContainer((Slot)new SlotInvSlot(base.resInputSlot, 0, 69, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(base.outputSlot, 0, 119, 35));
        this.addSlotToContainer((Slot)new SlotInvSlot(base.inputSlot, 0, 69, 53));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("progress");
        ret.add("mode");
        return ret;
    }
}
