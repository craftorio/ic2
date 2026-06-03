package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityFluidHeatGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerFluidHeatGenerator extends ContainerFullInv<TileEntityFluidHeatGenerator> {
  public ContainerFluidHeatGenerator(EntityPlayer player, TileEntityFluidHeatGenerator tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.fluidSlot, 0, 27, 21));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.outputSlot, 0, 27, 54));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("fluidTank");
    ret.add("transmitHeat");
    ret.add("maxHeatEmitpeerTick");
    return ret;
  }
}
