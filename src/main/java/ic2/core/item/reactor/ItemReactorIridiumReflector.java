// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactorComponent;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotClassic;

@NotClassic
public class ItemReactorIridiumReflector extends AbstractReactorComponent
{
    public ItemReactorIridiumReflector(final ItemName name) {
        super(name);
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        if (!heatrun) {
            final IReactorComponent source = (IReactorComponent)pulsingStack.getItem();
            source.acceptUraniumPulse(pulsingStack, reactor, stack, pulseX, pulseY, youX, youY, heatrun);
        }
        return true;
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return -1.0f;
    }
}
