package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerChargepadBlock extends ContainerFullInv<TileEntityChargepadBlock> {
  public ContainerChargepadBlock(EntityPlayer player, TileEntityChargepadBlock tileEntity1) {
    super(player, (IInventory)tileEntity1, 161);
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.chargeSlot, 0, 56, 17));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.dischargeSlot, 0, 56, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("redstoneMode");
    return ret;
  }
}
