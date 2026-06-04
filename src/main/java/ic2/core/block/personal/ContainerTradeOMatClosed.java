package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.slot.SlotInvSlotReadOnly;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerTradeOMatClosed extends ContainerFullInv<TileEntityTradeOMat> {
  public ContainerTradeOMatClosed(EntityPlayer player, TileEntityTradeOMat tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 19));
    addSlotToContainer((Slot)new SlotInvSlotReadOnly(tileEntity1.offerSlot, 0, 50, 38));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.inputSlot, 0, 143, 17));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.outputSlot, 0, 143, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("stock");
    return ret;
  }
}
