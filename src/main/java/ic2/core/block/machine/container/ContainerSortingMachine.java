// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.core.slot.SlotHologramSlot;
import net.minecraft.util.EnumFacing;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntitySortingMachine;

public class ContainerSortingMachine extends ContainerElectricMachine<TileEntitySortingMachine>
{
    public ContainerSortingMachine(final EntityPlayer player, final TileEntitySortingMachine tileEntity) {
        super(player, tileEntity, 243, 188, 219);
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.upgradeSlot, i, 188, 161 + i * 18));
        }
        for (int i = 0; i < 11; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.buffer, i, 8 + i * 18, 141));
        }
        for (int i = 0; i < EnumFacing.VALUES.length; ++i) {
            final EnumFacing dir = EnumFacing.VALUES[i];
            final ItemStack[] filterSlots = tileEntity.getFilterSlots(dir);
            for (int j = 0; j < filterSlots.length; ++j) {
                this.addSlotToContainer((Slot)new SlotHologramSlot(filterSlots, j, 80 + j * 18, 19 + i * 20, 64, null));
            }
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("defaultRoute");
        return ret;
    }
}
