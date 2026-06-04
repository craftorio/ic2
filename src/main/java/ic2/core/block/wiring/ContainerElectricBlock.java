// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.ArmorSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerElectricBlock extends ContainerFullInv<TileEntityElectricBlock>
{
    public ContainerElectricBlock(final EntityPlayer player, final TileEntityElectricBlock tileEntity1) {
        super(player, (IInventory)tileEntity1, 196);
        for (int col = 0; col < ArmorSlot.getCount(); ++col) {
            this.addSlotToContainer((Slot)new SlotArmor(player.inventory, ArmorSlot.get(col), 8 + col * 18, 84));
        }
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.chargeSlot, 0, 56, 17));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.dischargeSlot, 0, 56, 53));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("redstoneMode");
        return ret;
    }
}
