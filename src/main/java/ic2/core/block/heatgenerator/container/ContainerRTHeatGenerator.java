package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityRTHeatGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerRTHeatGenerator extends ContainerFullInv<TileEntityRTHeatGenerator> {
  public ContainerRTHeatGenerator(EntityPlayer player, TileEntityRTHeatGenerator tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    int i;
    for (i = 0; i < 3; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.fuelSlot, i, 62 + i * 18, 27)); 
    for (i = 3; i < 6; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.fuelSlot, i, 62 + (i - 3) * 18, 45)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("transmitHeat");
    ret.add("maxHeatEmitpeerTick");
    return ret;
  }
}
