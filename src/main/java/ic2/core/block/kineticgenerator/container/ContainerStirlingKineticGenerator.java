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
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.ContainerFullInv;

public class ContainerStirlingKineticGenerator extends ContainerFullInv<TileEntityStirlingKineticGenerator>
{
    public ContainerStirlingKineticGenerator(final EntityPlayer player, final TileEntityStirlingKineticGenerator te) {
        super(player, (IInventory)te, 204);
        this.addSlotToContainer((Slot)new SlotInvSlot(te.coolfluidinputSlot, 0, 8, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.cooloutputSlot, 0, 26, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.hotfluidinputSlot, 0, 134, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.hotoutputSlot, 0, 152, 103));
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(te.upgradeSlot, i, 62 + i * 18, 103));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("inputTank");
        ret.add("outputTank");
        return ret;
    }
}
