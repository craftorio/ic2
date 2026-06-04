// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityFluidRegulator;

public class ContainerFluidRegulator extends ContainerElectricMachine<TileEntityFluidRegulator>
{
    public ContainerFluidRegulator(final EntityPlayer player, final TileEntityFluidRegulator tileEntite) {
        super(player, tileEntite, 184, 8, 57);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.wasserinputSlot, 0, 58, 53));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.wasseroutputSlot, 0, 58, 71));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        ret.add("outputmb");
        ret.add("mode");
        return ret;
    }
}
