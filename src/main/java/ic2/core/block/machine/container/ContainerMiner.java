package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityMiner;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerMiner extends ContainerElectricMachine<TileEntityMiner> {
  public ContainerMiner(EntityPlayer player, TileEntityMiner tileEntity1) {
    super(player, tileEntity1, 166, 152, 58);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.scannerSlot, 0, 8, 58));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.pipeSlot, 0, 8, 40));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.drillSlot, 0, 8, 22));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntity1.upgradeSlot, 0, 152, 22));
    for (int y = 0; y < tileEntity1.buffer.size() / 5; y++) {
      for (int x = 0; x < 5; x++)
        func_75146_a((Slot)new SlotInvSlot(tileEntity1.buffer, x + y * 5, 44 + x * 18, 22 + y * 18)); 
    } 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("progress");
    return ret;
  }
}
