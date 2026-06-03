package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntitySolarDestiller;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerSolarDestiller extends ContainerFullInv<TileEntitySolarDestiller> {
  public ContainerSolarDestiller(EntityPlayer player, TileEntitySolarDestiller tileEntite) {
    super(player, (IInventory)tileEntite, 184);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.waterinputSlot, 0, 17, 27));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.destiwaterinputSlot, 0, 136, 64));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.wateroutputSlot, 0, 17, 45));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.destiwateroutputSlott, 0, 136, 82));
    for (int i = 0; i < 2; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.upgradeSlot, i, 152, 8 + i * 18)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("skyLight");
    ret.add("inputTank");
    ret.add("outputTank");
    return ret;
  }
}
