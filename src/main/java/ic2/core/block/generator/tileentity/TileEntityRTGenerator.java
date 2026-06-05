package ic2.core.block.generator.tileentity;

import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.init.MainConfig;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;

@NotClassic
public class TileEntityRTGenerator extends TileEntityBaseGenerator {
   public final InvSlotConsumable fuelSlot = new InvSlotConsumableItemStack(this, "fuel", 6, ItemName.nuclear.getItemStack(NuclearResourceType.rtg_pellet));
   private static final float efficiency = ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/radioisotope");

   public TileEntityRTGenerator() {
      super(Math.round(16.0F * efficiency), 1, 20000);
      this.fuelSlot.setStackSizeLimit(1);
   }

   @Override
   public boolean gainEnergy() {
      int counter = 0;

      for (int i = 0; i < this.fuelSlot.size(); i++) {
         if (!this.fuelSlot.isEmpty(i)) {
            counter++;
         }
      }

      if (counter == 0) {
         return false;
      }

      this.energy.addEnergy(Math.pow(2.0, counter - 1) * efficiency);
      return true;
   }

   @Override
   public boolean gainFuel() {
      return false;
   }

   @Override
   public boolean needsFuel() {
      return false;
   }

   @Override
   protected boolean delayActiveUpdate() {
      return true;
   }
}
