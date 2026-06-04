package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityFermenter;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerFermenter extends ContainerFullInv<TileEntityFermenter> {
  public ContainerFermenter(EntityPlayer player, TileEntityFermenter te) {
    super(player, (IInventory)te, 184);
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.fluidInputCellInSlot, 0, 14, 46));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.fluidInputCellOutSlot, 0, 14, 64));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.fluidOutputCellInSlot, 0, 148, 43));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.fluidOutputCellOutSlot, 0, 148, 61));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.fertiliserSlot, 0, 86, 83));
    for (int i = 0; i < 2; i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)te.upgradeSlot, i, 125 + i * 18, 83)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("inputTank");
    ret.add("outputTank");
    ret.add("progress");
    ret.add("heatBuffer");
    return ret;
  }
}
