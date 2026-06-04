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
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.ContainerFullInv;

public class ContainerElectricHeatGenerator extends ContainerFullInv<TileEntityElectricHeatGenerator>
{
    public ContainerElectricHeatGenerator(final EntityPlayer player, final TileEntityElectricHeatGenerator tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        for (int i = 0; i < 5; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.coilSlot, i, 44 + i * 18, 27));
        }
        for (int i = 5; i < 10; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.coilSlot, i, 44 + (i - 5) * 18, 45));
        }
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.dischargeSlot, 0, 8, 62));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("transmitHeat");
        ret.add("maxHeatEmitpeerTick");
        return ret;
    }
}
