package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.item.type.NuclearResourceType;
import ic2.core.profile.NotExperimental;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

@NotExperimental
public class ItemReactorDepletedUranium extends AbstractDamageableReactorComponent {
   public ItemReactorDepletedUranium() {
      super(ItemName.depleted_isotope_fuel_rod, 10000);
   }

   @Override
   public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
      if (heatrun) {
         int myLevel = this.getCustomDamage(stack) + 1 + reactor.getHeat() / 3000;
         if (myLevel >= this.getMaxCustomDamage(stack)) {
            reactor.setItemAt(youX, youY, ItemName.nuclear.getItemStack(NuclearResourceType.re_enriched_uranium));
         } else {
            this.setCustomDamage(stack, myLevel);
         }
      }

      return true;
   }

   @Override
   public double getDurabilityForDisplay(ItemStack stack) {
      return 1.0 - super.getDurabilityForDisplay(stack);
   }
}
