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
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.ContainerFullInv;

public class ContainerLiquidHeatExchanger extends ContainerFullInv<TileEntityLiquidHeatExchanger>
{
    public ContainerLiquidHeatExchanger(final EntityPlayer player, final TileEntityLiquidHeatExchanger tileEntite) {
        super(player, (IInventory)tileEntite, 204);
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.hotfluidinputSlot, 0, 8, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.cooloutputSlot, 0, 152, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.coolfluidinputSlot, 0, 134, 103));
        this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.hotoutputSlot, 0, 26, 103));
        for (int i = 0; i < 3; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.upgradeSlot, i, 62 + i * 18, 103));
        }
        for (int i = 0; i < 5; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.heatexchangerslots, i, 46 + i * 17, 50));
        }
        for (int i = 5; i < 10; ++i) {
            this.addSlotToContainer((Slot)new SlotInvSlot(tileEntite.heatexchangerslots, i, 46 + (i - 5) * 17, 72));
        }
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("inputTank");
        ret.add("outputTank");
        ret.add("transmitHeat");
        ret.add("maxHeatEmitpeerTick");
        return ret;
    }
}
