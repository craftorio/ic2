// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.machine.tileentity.TileEntityStandardMachine;

public class ContainerStandardMachine<T extends TileEntityStandardMachine<?, ?, ?>> extends ContainerElectricMachine<T>
{
    public ContainerStandardMachine(final EntityPlayer player, final T tileEntity1) {
        this(player, tileEntity1, 166, 56, 53, 56, 17, 116, 35, 152, 8);
    }
    
    public ContainerStandardMachine(final EntityPlayer player, final T tileEntity1, final int height, final int dischargeX, final int dischargeY, final int inputX, final int inputY, final int outputX, final int outputY, final int upgradeX, final int upgradeY) {
        super(player, tileEntity1, height, dischargeX, dischargeY);
        if (tileEntity1.inputSlot != null) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.inputSlot, 0, inputX, inputY));
        }
        if (tileEntity1.outputSlot != null) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.outputSlot, 0, outputX, outputY));
        }
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.upgradeSlot, i, upgradeX, upgradeY + i * 18));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("guiProgress");
        return ret;
    }
}
