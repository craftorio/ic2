package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityWeightedFluidDistributor;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerWeightedFluidDistributor extends ContainerFullInv<TileEntityWeightedFluidDistributor> {
  public static final short HEIGHT = 211;
  
  public ContainerWeightedFluidDistributor(EntityPlayer player, TileEntityWeightedFluidDistributor te) {
    super(player, (IInventory)te, 211);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.inputSlot, 0, 8, 108));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.OutputSlot, 0, 152, 108));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("fluidTank");
    return ret;
  }
}
