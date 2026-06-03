package ic2.core.block.personal;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerTradingTerminal extends ContainerFullInv<TileEntityTradingTerminal> {
  public final Slot rangeSlot = func_75146_a((Slot)new SlotInvSlot((InvSlot)((TileEntityTradingTerminal)this.base).rangeUpgrade, 0, -100, -100));
  
  public ContainerTradingTerminal(EntityPlayer player, TileEntityTradingTerminal base) {
    super(player, (IInventory)base, 176, 227);
  }
  
  public List<String> getNetworkedFields() {
    List<String> out = super.getNetworkedFields();
    out.add("range");
    return out;
  }
}
