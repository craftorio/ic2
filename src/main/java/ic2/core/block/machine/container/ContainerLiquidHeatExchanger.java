package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityLiquidHeatExchanger;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerLiquidHeatExchanger extends ContainerFullInv<TileEntityLiquidHeatExchanger> {
  public ContainerLiquidHeatExchanger(EntityPlayer player, TileEntityLiquidHeatExchanger tileEntite) {
    super(player, (IInventory)tileEntite, 204);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.hotfluidinputSlot, 0, 8, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.cooloutputSlot, 0, 152, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.coolfluidinputSlot, 0, 134, 103));
    func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.hotoutputSlot, 0, 26, 103));
    int i;
    for (i = 0; i < 3; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.upgradeSlot, i, 62 + i * 18, 103)); 
    for (i = 0; i < 5; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.heatexchangerslots, i, 46 + i * 17, 50)); 
    for (i = 5; i < 10; i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)tileEntite.heatexchangerslots, i, 46 + (i - 5) * 17, 72)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("inputTank");
    ret.add("outputTank");
    ret.add("transmitHeat");
    ret.add("maxHeatEmitpeerTick");
    return ret;
  }
}
