// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityFluidBottler;

public class ContainerFluidBottler extends ContainerStandardMachine<TileEntityFluidBottler>
{
    public ContainerFluidBottler(final EntityPlayer player, final TileEntityFluidBottler tileEntity1) {
        super(player, tileEntity1, 184, 8, 53, 0, 0, 117, 53, 152, 26);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.drainInputSlot, 0, 44, 35));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.fillInputSlot, 0, 44, 72));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        return ret;
    }
}
