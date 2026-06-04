// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.personal;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import ic2.core.ContainerFullInv;

public class ContainerTradingTerminal extends ContainerFullInv<TileEntityTradingTerminal>
{
    public final Slot rangeSlot;
    
    public ContainerTradingTerminal(final EntityPlayer player, final TileEntityTradingTerminal base) {
        super(player, (IInventory)base, 176, 227);
        this.rangeSlot = this.addSlotToContainer((Slot)new SlotInvSlot(((TileEntityTradingTerminal)this.base).rangeUpgrade, 0, -100, -100));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> out = super.getNetworkedFields();
        out.add("range");
        return out;
    }
}
