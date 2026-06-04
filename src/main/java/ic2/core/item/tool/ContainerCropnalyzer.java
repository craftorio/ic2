// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.slot.SlotDischarge;
import net.minecraft.item.Item;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.IInventory;
import ic2.core.slot.SlotCustom;
import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.item.ContainerHandHeldInventory;

public class ContainerCropnalyzer extends ContainerHandHeldInventory<HandHeldCropnalyzer>
{
    public ContainerCropnalyzer(final EntityPlayer player, final HandHeldCropnalyzer cropnalyzer1) {
        super(cropnalyzer1);
        this.addSlotToContainer((Slot)new SlotCustom((IInventory)cropnalyzer1, ItemName.crop_seed_bag.getInstance(), 0, 8, 7));
        this.addSlotToContainer((Slot)new SlotCustom((IInventory)cropnalyzer1, null, 1, 41, 7));
        this.addSlotToContainer((Slot)new SlotDischarge((IInventory)cropnalyzer1, 2, 152, 7));
        this.addPlayerInventorySlots(player, 223);
    }
}
