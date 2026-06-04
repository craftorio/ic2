// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityScanner;

public class ContainerScanner extends ContainerElectricMachine<TileEntityScanner>
{
    public ContainerScanner(final EntityPlayer player, final TileEntityScanner tileEntity1) {
        super(player, tileEntity1, 166, 8, 43);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.inputSlot, 0, 55, 35));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.diskSlot, 0, 152, 65));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("state");
        ret.add("progress");
        ret.add("patternEu");
        ret.add("patternUu");
        return ret;
    }
}
