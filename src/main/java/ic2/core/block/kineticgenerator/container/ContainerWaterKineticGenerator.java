// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.kineticgenerator.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWaterKineticGenerator;
import ic2.core.ContainerFullInv;

public class ContainerWaterKineticGenerator extends ContainerFullInv<TileEntityWaterKineticGenerator>
{
    public ContainerWaterKineticGenerator(final EntityPlayer player, final TileEntityWaterKineticGenerator tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.rotorSlot, 0, 80, 26));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("waterFlow");
        ret.add("type");
        return ret;
    }
}
