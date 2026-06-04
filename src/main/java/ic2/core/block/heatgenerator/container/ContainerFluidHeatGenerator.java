// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.heatgenerator.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.ContainerFullInv;

public class ContainerFluidHeatGenerator extends ContainerFullInv<TileEntityFluidHeatGenerator>
{
    public ContainerFluidHeatGenerator(final EntityPlayer player, final TileEntityFluidHeatGenerator tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.fluidSlot, 0, 27, 21));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, 27, 54));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("fluidTank");
        ret.add("transmitHeat");
        ret.add("maxHeatEmitpeerTick");
        return ret;
    }
}
