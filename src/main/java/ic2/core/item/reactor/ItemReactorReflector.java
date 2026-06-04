// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorReflector extends AbstractDamageableReactorComponent
{
    public ItemReactorReflector(final ItemName name, final int maxDamage) {
        super(name, maxDamage);
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        if (!heatrun) {
            final IReactorComponent source = (IReactorComponent)pulsingStack.getItem();
            source.acceptUraniumPulse(pulsingStack, reactor, stack, pulseX, pulseY, youX, youY, heatrun);
        }
        else if (this.getCustomDamage(stack) + 1 >= this.getMaxCustomDamage(stack)) {
            reactor.setItemAt(youX, youY, null);
        }
        else {
            this.setCustomDamage(stack, this.getCustomDamage(stack) + 1);
        }
        return true;
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return -1.0f;
    }
}
