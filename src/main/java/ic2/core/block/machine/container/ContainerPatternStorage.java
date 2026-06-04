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
import ic2.core.block.machine.tileentity.TileEntityPatternStorage;
import ic2.core.ContainerFullInv;

public class ContainerPatternStorage extends ContainerFullInv<TileEntityPatternStorage>
{
    public ContainerPatternStorage(final EntityPlayer player, final TileEntityPatternStorage tileEntity1) {
        super(player, (IInventory)tileEntity1, 166);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.diskSlot, 0, 18, 20));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("index");
        ret.add("maxIndex");
        ret.add("pattern");
        ret.add("patternUu");
        ret.add("patternEu");
        return ret;
    }
}
