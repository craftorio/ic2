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
import ic2.core.block.machine.tileentity.TileEntityMatter;
import ic2.core.ContainerFullInv;

public class ContainerMatter extends ContainerFullInv<TileEntityMatter>
{
    public ContainerMatter(final EntityPlayer player, final TileEntityMatter tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.amplifierSlot, 0, 72, 40));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, 125, 59));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.containerslot, 0, 125, 23));
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.upgradeSlot, i, 152, 8 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("energy");
        ret.add("scrap");
        ret.add("fluidTank");
        return ret;
    }
}
