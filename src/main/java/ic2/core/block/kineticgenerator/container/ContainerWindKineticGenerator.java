package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.kineticgenerator.tileentity.TileEntityWindKineticGenerator;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerWindKineticGenerator extends ContainerFullInv<TileEntityWindKineticGenerator> {
  public ContainerWindKineticGenerator(EntityPlayer player, TileEntityWindKineticGenerator tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.rotorSlot, 0, 80, 26));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("windStrength");
    return ret;
  }
}
