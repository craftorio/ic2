package ic2.core.item.tool;

import ic2.core.item.ContainerHandHeldInventory;
import ic2.core.slot.SlotRadioactive;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerContainmentbox extends ContainerHandHeldInventory<HandHeldContainmentbox> {
  protected static final int height = 166;
  
  public ContainerContainmentbox(EntityPlayer player, HandHeldContainmentbox box) {
    super(box);
    int i;
    for (i = 0; i < 4; i++)
      addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + i * 18, 19)); 
    for (i = 4; i < 8; i++)
      addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + (i - 4) * 18, 37)); 
    for (i = 8; i < 12; i++)
      addSlotToContainer((Slot)new SlotRadioactive((IInventory)box, i, 53 + (i - 8) * 18, 55)); 
    addPlayerInventorySlots(player, 166);
  }
}
