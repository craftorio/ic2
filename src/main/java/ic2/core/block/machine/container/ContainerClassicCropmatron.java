package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityClassicCropmatron;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerClassicCropmatron extends ContainerFullInv<TileEntityClassicCropmatron> {
  public ContainerClassicCropmatron(EntityPlayer player, TileEntityClassicCropmatron base) {
    super(player, (IInventory)base, 166);
    int i;
    for (i = 0; i < base.fertilizerSlot.size(); i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)base.fertilizerSlot, i, 62, 20 + i * 18)); 
    for (i = 0; i < base.hydrationSlot.size(); i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)base.hydrationSlot, i, 98, 20 + i * 18)); 
    for (i = 0; i < base.weedExSlot.size(); i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)base.weedExSlot, i, 134, 20 + i * 18)); 
  }
}
