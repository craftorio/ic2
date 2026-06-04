package ic2.core.block.wiring;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.ArmorSlot;
import ic2.core.slot.SlotArmor;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerElectricBlock extends ContainerFullInv<TileEntityElectricBlock> {
  public ContainerElectricBlock(EntityPlayer player, TileEntityElectricBlock tileEntity1) {
    super(player, (IInventory)tileEntity1, 196);
    for (int col = 0; col < ArmorSlot.getCount(); col++)
      func_75146_a((Slot)new SlotArmor(player.inventory, 
            ArmorSlot.get(col), 8 + col * 18, 84)); 
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.chargeSlot, 0, 56, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.dischargeSlot, 0, 56, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("redstoneMode");
    return ret;
  }
}
