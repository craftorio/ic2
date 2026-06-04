package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityAdvMiner;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerAdvMiner extends ContainerElectricMachine<TileEntityAdvMiner> {
  public ContainerAdvMiner(EntityPlayer player, TileEntityAdvMiner tileEntity1) {
    super(player, tileEntity1, 203, 8, 80);
    addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.scannerSlot, 0, 8, 26));
    for (int i = 0; i < 4; i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity1.upgradeSlot, i, 152, 26 + i * 18)); 
    for (int row = 0; row < 3; row++) {
      for (int col = 0; col < 5; col++)
        addSlotToContainer((Slot)new SlotInvSlot(tileEntity1.filterSlot, col + row * 5, 36 + col * 18, 44 + row * 18)); 
    } 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("mineTarget");
    ret.add("blacklist");
    ret.add("silkTouch");
    return ret;
  }
}
