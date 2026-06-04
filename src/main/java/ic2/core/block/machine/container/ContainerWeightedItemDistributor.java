package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityWeightedItemDistributor;
import ic2.core.slot.SlotInvSlot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerWeightedItemDistributor extends ContainerFullInv<TileEntityWeightedItemDistributor> {
  public static final short HEIGHT = 211;
  
  public ContainerWeightedItemDistributor(EntityPlayer player, TileEntityWeightedItemDistributor te) {
    super(player, (IInventory)te, 211);
    for (int i = 0; i < te.buffer.size(); i++)
      addSlotToContainer((Slot)new SlotInvSlot(te.buffer, i, 8 + i * 18, 108)); 
  }
}
