// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import net.minecraft.inventory.Slot;
import net.minecraft.inventory.IInventory;
import ic2.core.slot.SlotRadioactive;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.item.ContainerHandHeldInventory;

public class ContainerContainmentbox extends ContainerHandHeldInventory<HandHeldContainmentbox>
{
    protected static final int height = 166;
    
    public ContainerContainmentbox(final EntityPlayer player, final HandHeldContainmentbox box) {
        super(box);
        for (int i = 0; i < 4; ++i) {
            this.addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + i * 18, 19));
        }
        for (int i = 4; i < 8; ++i) {
            this.addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + (i - 4) * 18, 37));
        }
        for (int i = 8; i < 12; ++i) {
            this.addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + (i - 8) * 18, 55));
        }
        this.addPlayerInventorySlots(player, 166);
    }
}
