package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerStirlingKineticGenerator extends ContainerFullInv<TileEntityStirlingKineticGenerator> {
  public ContainerStirlingKineticGenerator(EntityPlayer player, TileEntityStirlingKineticGenerator te) {
    super(player, (IInventory)te, 204);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.coolfluidinputSlot, 0, 8, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.cooloutputSlot, 0, 26, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.hotfluidinputSlot, 0, 134, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)te.hotoutputSlot, 0, 152, 103));
    for (int i = 0; i < 3; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)te.upgradeSlot, i, 62 + i * 18, 103)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("inputTank");
    ret.add("outputTank");
    return ret;
  }
}
