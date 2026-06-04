// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;

public class ContainerAdvMiner extends ContainerElectricMachine<TileEntityAdvMiner>
{
    public ContainerAdvMiner(final EntityPlayer player, final TileEntityAdvMiner tileEntity1) {
        super(player, tileEntity1, 203, 8, 80);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.scannerSlot, 0, 8, 26));
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.upgradeSlot, i, 152, 26 + i * 18));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 5; ++col) {
                this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.filterSlot, col + row * 5, 36 + col * 18, 44 + row * 18));
            }
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("mineTarget");
        ret.add("blacklist");
        ret.add("silkTouch");
        return ret;
    }
}
