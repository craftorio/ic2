package ic2.core.block.kineticgenerator.container;

import ic2.core.ContainerFullInv;
import ic2.core.block.kineticgenerator.tileentity.TileEntityStirlingKineticGenerator;
import ic2.core.slot.SlotInvSlot;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;

public class ContainerStirlingKineticGenerator extends ContainerFullInv<TileEntityStirlingKineticGenerator> {
   public ContainerStirlingKineticGenerator(EntityPlayer player, TileEntityStirlingKineticGenerator te) {
      super(player, te, 204);
      this.addSlotToContainer(new SlotInvSlot(te.coolfluidinputSlot, 0, 8, 103));
      this.addSlotToContainer(new SlotInvSlot(te.cooloutputSlot, 0, 26, 103));
      this.addSlotToContainer(new SlotInvSlot(te.hotfluidinputSlot, 0, 134, 103));
      this.addSlotToContainer(new SlotInvSlot(te.hotoutputSlot, 0, 152, 103));

      for (int i = 0; i < 3; i++) {
         this.addSlotToContainer(new SlotInvSlot(te.upgradeSlot, i, 62 + i * 18, 103));
      }
   }

   @Override
   public List<String> getNetworkedFields() {
      List<String> ret = super.getNetworkedFields();
      ret.add("inputTank");
      ret.add("outputTank");
      return ret;
   }
}
