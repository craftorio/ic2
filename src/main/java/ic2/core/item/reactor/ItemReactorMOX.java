package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemReactorMOX extends ItemReactorUranium {
   public ItemReactorMOX(ItemName name, int cells) {
      super(name, cells, 10000);
   }

   @Override
   protected int getFinalHeat(ItemStack stack, IReactor reactor, int x, int y, int heat) {
      if (reactor.isFluidCooled()) {
         float breedereffectiveness = (float)reactor.getHeat() / reactor.getMaxHeat();
         if (breedereffectiveness > 0.5) {
            heat *= 2;
         }
      }

      return heat;
   }

   @Override
   protected ItemStack getDepletedStack(ItemStack stack, IReactor reactor) {
      ItemStack ret;
      switch (this.numberOfCells) {
         case 1:
            ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_mox);
            break;
         case 2:
            ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_dual_mox);
            break;
         case 3:
         default:
            throw new RuntimeException("invalid cell count: " + this.numberOfCells);
         case 4:
            ret = ItemName.nuclear.getItemStack(NuclearResourceType.depleted_quad_mox);
      }

      return ret.copy();
   }

   @Override
   public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
      if (!heatrun) {
         float breedereffectiveness = (float)reactor.getHeat() / reactor.getMaxHeat();
         float ReaktorOutput = 4.0F * breedereffectiveness + 1.0F;
         reactor.addOutput(ReaktorOutput);
      }

      return true;
   }
}
