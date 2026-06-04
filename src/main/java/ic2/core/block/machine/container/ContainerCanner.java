// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityCanner;

public class ContainerCanner extends ContainerStandardMachine<TileEntityCanner>
{
    public ContainerCanner(final EntityPlayer player, final TileEntityCanner tileEntity1) {
        super(player, tileEntity1, 184, 8, 80, 80, 44, 119, 17, 152, 26);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.canInputSlot, 0, 41, 17));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("mode");
        ret.add("inputTank");
        ret.add("outputTank");
        return ret;
    }
}
