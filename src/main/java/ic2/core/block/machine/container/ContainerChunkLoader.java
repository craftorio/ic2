package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;

public class ContainerChunkLoader extends ContainerFullInv<TileEntityChunkloader> {
  public ContainerChunkLoader(EntityPlayer player, TileEntityChunkloader base1) {
    super(player, (IInventory)base1, 250);
    func_75146_a((Slot)new SlotInvSlot((InvSlot)base1.dischargeSlot, 0, 8, 143));
    for (int i = 0; i < base1.upgradeSlot.size(); i++)
      func_75146_a((Slot)new SlotInvSlot((InvSlot)base1.upgradeSlot, i, 8, 44 + 18 * i)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("loadedChunks");
    return ret;
  }
}
