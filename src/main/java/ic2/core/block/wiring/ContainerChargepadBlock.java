// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.wiring;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.ContainerFullInv;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargepadBlock>
{
    public ContainerChargepadBlock(final EntityPlayer player, final TileEntityChargepadBlock tileEntity1) {
        super(player, (IInventory)tileEntity1, 161);
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
