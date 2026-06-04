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
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.ContainerFullInv;

public class ContainerFluidDistributor extends ContainerFullInv<TileEntityFluidDistributor>
{
    public ContainerFluidDistributor(final EntityPlayer player, final TileEntityFluidDistributor tileEntite) {
        super(player, (IInventory)tileEntite, 184);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.inputSlot, 0, 9, 54));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.OutputSlot, 0, 9, 72));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        return ret;
    }
}
