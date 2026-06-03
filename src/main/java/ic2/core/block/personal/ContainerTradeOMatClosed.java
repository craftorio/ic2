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
    func_75146_a((Slot)new SlotInvSlotReadOnly(tileEntity1.demandSlot, 0, 50, 19));
    func_75146_a((Slot)new SlotInvSlotReadOnly(tileEntity1.offerSlot, 0, 50, 38));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.inputSlot, 0, 143, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.outputSlot, 0, 143, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("stock");
    return ret;
  }
}
