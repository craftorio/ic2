package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityCondenser;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerCondenser extends ContainerElectricMachine<TileEntityCondenser> {
  public ContainerCondenser(EntityPlayer player, TileEntityCondenser te) {
    super(player, te, 184, 8, 44);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.waterInputSlot, 0, 26, 73));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.waterOutputSlot, 0, 134, 73));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.upgradeSlot, 0, 152, 73));
    for (int i = 0; i < 2; i++) {
      func_75146_a((Slot)new SlotInvSlot((InvSlot)te.ventSlots, i, 26 + i * 108, 26));
      func_75146_a((Slot)new SlotInvSlot((InvSlot)te.ventSlots, i + 2, 26 + i * 108, 44));
    } 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("inputTank");
    ret.add("outputTank");
    ret.add("progress");
    return ret;
  }
}
