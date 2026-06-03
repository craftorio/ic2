package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityCanner;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerCanner extends ContainerStandardMachine<TileEntityCanner> {
  public ContainerCanner(EntityPlayer player, TileEntityCanner tileEntity1) {
    super(player, tileEntity1, 184, 8, 80, 80, 44, 119, 17, 152, 26);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.canInputSlot, 0, 41, 17));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("mode");
    ret.add("inputTank");
    ret.add("outputTank");
    return ret;
  }
}
