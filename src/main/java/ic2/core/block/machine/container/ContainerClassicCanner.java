package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityClassicCanner;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerClassicCanner extends ContainerElectricMachine<TileEntityClassicCanner> {
  public ContainerClassicCanner(EntityPlayer player, TileEntityClassicCanner base) {
    super(player, base, 166, 30, 45);
    func_75146_a((Slot)new SlotInvSlot(base.resInputSlot, 0, 69, 17));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)base.outputSlot, 0, 119, 35));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)base.inputSlot, 0, 69, 53));
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("progress");
    ret.add("mode");
    return ret;
  }
}
