// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;

public class ContainerElectrolyzer extends ContainerElectricMachine<TileEntityElectrolyzer>
{
    public ContainerElectrolyzer(final EntityPlayer player, final TileEntityElectrolyzer tileEntity) {
        super(player, tileEntity, 166, 8, 62);
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity.upgradeSlot, i, 152, 8 + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("progress");
        ret.add("recipe");
        ret.add("input");
        return ret;
    }
}
