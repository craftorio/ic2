package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.ref.ItemName;
import ic2.core.slot.SlotCustom;
import ic2.core.slot.SlotDischarge;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerCropnalyzer extends ContainerHandHeldInventory<HandHeldCropnalyzer> {
  public ContainerCropnalyzer(EntityPlayer player, HandHeldCropnalyzer cropnalyzer1) {
    super(cropnalyzer1);
    addSlotToContainer((Slot)new SlotCustom((IInventory)cropnalyzer1, ItemName.crop_seed_bag.getInstance(), 0, 8, 7));
    addSlotToContainer((Slot)new SlotCustom((IInventory)cropnalyzer1, null, 1, 41, 7));
    addSlotToContainer((Slot)new SlotDischarge((IInventory)cropnalyzer1, 2, 152, 7));
    addPlayerInventorySlots(player, 223);
  }
}
