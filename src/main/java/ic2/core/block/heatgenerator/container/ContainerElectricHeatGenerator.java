package ic2.core.block.heatgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.heatgenerator.tileentity.TileEntityElectricHeatGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerElectricHeatGenerator extends ContainerFullInv<TileEntityElectricHeatGenerator> {
  public ContainerElectricHeatGenerator(EntityPlayer player, TileEntityElectricHeatGenerator tileEntity1) {
    super(player, (IInventory)tileEntity1, 166);
    int i;
    for (i = 0; i < 5; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.coilSlot, i, 44 + i * 18, 27)); 
    for (i = 5; i < 10; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.coilSlot, i, 44 + (i - 5) * 18, 45)); 
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.dischargeSlot, 0, 8, 62));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("transmitHeat");
    ret.add("maxHeatEmitpeerTick");
    return ret;
  }
}
