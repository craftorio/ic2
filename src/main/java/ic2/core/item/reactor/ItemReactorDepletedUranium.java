// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.core.item.type.NuclearResourceType;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotExperimental;

@NotExperimental
public class ItemReactorDepletedUranium extends AbstractDamageableReactorComponent
{
    public ItemReactorDepletedUranium() {
        super(ItemName.depleted_isotope_fuel_rod, 10000);
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        if (heatrun) {
            final int myLevel = this.getCustomDamage(stack) + 1 + reactor.getHeat() / 3000;
            if (myLevel >= this.getMaxCustomDamage(stack)) {
                reactor.setItemAt(youX, youY, ItemName.nuclear.getItemStack(NuclearResourceType.re_enriched_uranium));
            }
            else {
                this.setCustomDamage(stack, myLevel);
            }
        }
        return true;
    }
    
    @Override
    public double getDurabilityForDisplay(final ItemStack stack) {
        return 1.0 - super.getDurabilityForDisplay(stack);
    }
}
