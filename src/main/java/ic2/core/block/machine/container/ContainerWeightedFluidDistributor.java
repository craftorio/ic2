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
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.ContainerFullInv;

public class ContainerWeightedFluidDistributor extends ContainerFullInv<TileEntityWeightedFluidDistributor>
{
    public static final short HEIGHT = 211;
    
    public ContainerWeightedFluidDistributor(final EntityPlayer player, final TileEntityWeightedFluidDistributor te) {
        super(player, (IInventory)te, 211);
        this.addSlotToContainer((Slot)new SlotInvSlot(te.inputSlot, 0, 8, 108));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.OutputSlot, 0, 152, 108));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        return ret;
    }
}
