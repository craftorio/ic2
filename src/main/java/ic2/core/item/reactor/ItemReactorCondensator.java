// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorCondensator extends AbstractDamageableReactorComponent
{
    public ItemReactorCondensator(final ItemName name, final int maxdmg) {
        super(name, maxdmg);
    }
    
    @Override
    public boolean canStoreHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return this.getCurrentHeat(stack) < this.getMaxCustomDamage(stack);
    }
    
    @Override
    public int getMaxHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return this.getMaxCustomDamage(stack);
    }
    
    private int getCurrentHeat(final ItemStack stack) {
        return this.getCustomDamage(stack);
    }
    
    @Override
    public int alterHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, int heat) {
        if (heat < 0) {
            return heat;
        }
        final int currentHeat = this.getCurrentHeat(stack);
        final int amount = Math.min(heat, this.getMaxHeat(stack, reactor, x, y) - currentHeat);
        heat -= amount;
        this.setCustomDamage(stack, currentHeat + amount);
        return heat;
    }
}
