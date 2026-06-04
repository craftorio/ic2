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
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.ContainerFullInv;

public class ContainerChunkLoader extends ContainerFullInv<TileEntityChunkloader>
{
    public ContainerChunkLoader(final EntityPlayer player, final TileEntityChunkloader base1) {
        super(player, (IInventory)base1, 250);
        this.addSlotToContainer((Slot)new SlotInvSlot(base1.dischargeSlot, 0, 8, 143));
        for (int i = 0; i < base1.upgradeSlot.size(); ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base1.upgradeSlot, i, 8, 44 + 18 * i));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("loadedChunks");
        return ret;
    }
}
