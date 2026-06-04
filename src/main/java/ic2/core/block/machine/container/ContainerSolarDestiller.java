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
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.ContainerFullInv;

public class ContainerSolarDestiller extends ContainerFullInv<TileEntitySolarDestiller>
{
    public ContainerSolarDestiller(final EntityPlayer player, final TileEntitySolarDestiller tileEntite) {
        super(player, (IInventory)tileEntite, 184);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.waterinputSlot, 0, 17, 27));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.destiwaterinputSlot, 0, 136, 64));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.wateroutputSlot, 0, 17, 45));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.destiwateroutputSlott, 0, 136, 82));
        for (int i = 0; i < 2; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.upgradeSlot, i, 152, 8 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("skyLight");
        ret.add("inputTank");
        ret.add("outputTank");
        return ret;
    }
}
