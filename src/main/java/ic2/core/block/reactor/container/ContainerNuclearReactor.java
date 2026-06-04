// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.reactor.container;

import java.util.List;
import net.minecraft.inventory.Slot;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.block.reactor.tileentity.TileEntityNuclearReactorElectric;
import ic2.core.ContainerBase;

public class ContainerNuclearReactor extends ContainerBase<TileEntityNuclearReactorElectric>
{
    private final int size;
    
    public ContainerNuclearReactor(final EntityPlayer player, final TileEntityNuclearReactorElectric te) {
        super((IInventory)te);
        this.size = te.getReactorSize();
        final int startX = 26;
        final int startY = 25;
        for (int slotCount = te.reactorSlot.size(), i = 0; i < slotCount; ++i) {
            final int x = i % this.size;
            final int y = i / this.size;
            this.addSlotToContainer((Slot)new SlotInvSlot(te.reactorSlot, i, startX + 18 * x, startY + 18 * y));
        }
        this.addPlayerInventorySlots(player, 214, 243);
        this.addSlotToContainer((Slot)new SlotInvSlot(te.coolantinputSlot, 0, 8, 25));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.hotcoolinputSlot, 0, 188, 25));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.coolantoutputSlot, 0, 8, 115));
        this.addSlotToContainer((Slot)new SlotInvSlot(te.hotcoolantoutputSlot, 0, 188, 115));
    }
    
    @Override
    public List<String> getNetworkedFields() {
        final List<String> ret = super.getNetworkedFields();
        ret.add("heat");
        ret.add("maxHeat");
        ret.add("EmitHeat");
        ret.add("inputTank");
        ret.add("outputTank");
        ret.add("fluidCooled");
        return ret;
    }
}
