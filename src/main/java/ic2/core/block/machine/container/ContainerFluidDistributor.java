package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityFluidDistributor;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerFluidDistributor extends ContainerFullInv<TileEntityFluidDistributor> {
  public ContainerFluidDistributor(EntityPlayer player, TileEntityFluidDistributor tileEntite) {
    super(player, (IInventory)tileEntite, 184);
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntite.inputSlot, 0, 9, 54));
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntite.outputSlot, 0, 9, 72));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("fluidTank");
    return ret;
  }
}
