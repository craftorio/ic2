// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import ic2.core.block.invslot.InvSlot;
import net.minecraft.inventory.Slot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityCropHarvester;

public class ContainerCropHarvester extends ContainerElectricMachine<TileEntityCropHarvester>
{
    public ContainerCropHarvester(final EntityPlayer player, final TileEntityCropHarvester base) {
        super(player, base, 166, 16, 53);
        for (int y = 0; y < base.contentSlot.size() / 5; ++y) {
            for (int x = 0; x < 5; ++x) {
                this.addSlotToContainer((Slot)new SlotInvSlot(base.contentSlot, x + y * 5, 48 + x * 18, 17 + y * 18));
            }
        }
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(base.upgradeSlot, i, 152, 8 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("energy");
        return ret;
    }
}
