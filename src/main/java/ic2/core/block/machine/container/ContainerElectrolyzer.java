package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityElectrolyzer;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerElectrolyzer extends ContainerElectricMachine<TileEntityElectrolyzer> {
  public ContainerElectrolyzer(EntityPlayer player, TileEntityElectrolyzer tileEntity) {
    super(player, tileEntity, 166, 8, 62);
    for (int i = 0; i < 4; i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)tileEntity.upgradeSlot, i, 152, 8 + i * 18)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("progress");
    ret.add("recipe");
    ret.add("input");
    return ret;
  }
}
