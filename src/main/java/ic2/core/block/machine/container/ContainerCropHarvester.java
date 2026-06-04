package ic2.core.block.machine.container;

import ic2.core.block.invslot.InvSlot;
import ic2.core.block.machine.tileentity.TileEntityCropHarvester;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;

public class ContainerCropHarvester extends ContainerElectricMachine<TileEntityCropHarvester> {
  public ContainerCropHarvester(EntityPlayer player, TileEntityCropHarvester base) {
    super(player, base, 166, 16, 53);
    for (int y = 0; y < base.contentSlot.size() / 5; y++) {
      for (int x = 0; x < 5; x++)
        addSlotToContainer((Slot)new SlotInvSlot(base.contentSlot, x + y * 5, 48 + x * 18, 17 + y * 18)); 
    } 
    for (int i = 0; i < 4; i++)
      addSlotToContainer((Slot)new SlotInvSlot((InvSlot)base.upgradeSlot, i, 152, 8 + i * 18)); 
  }
  
  public List<String> getNetworkedFields() {
    List<String> ret = super.getNetworkedFields();
    ret.add("energy");
    return ret;
  }
}
