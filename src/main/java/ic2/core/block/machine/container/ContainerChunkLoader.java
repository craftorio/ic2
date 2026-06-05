package ic2.core.block.machine.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.machine.tileentity.TileEntityChunkloader;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerChunkLoader extends ContainerFullInv<TileEntityChunkloader> {
   public ContainerChunkLoader(EntityPlayer player, TileEntityChunkloader base1) {
      super(player, base1, 250);
      this.addSlotToContainer(new SlotInvSlot(base1.dischargeSlot, 0, 8, 143));

      for (int i = 0; i < base1.upgradeSlot.size(); i++) {
         this.addSlotToContainer(new SlotInvSlot(base1.upgradeSlot, i, 8, 44 + 18 * i));
      }
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = super.getNetworkedFields();
      ret.add("loadedChunks");
      return ret;
   }
}
