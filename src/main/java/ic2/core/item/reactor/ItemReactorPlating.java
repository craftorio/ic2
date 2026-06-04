// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorPlating extends AbstractReactorComponent
{
    private final int maxHeatAdd;
    private final float effectModifier;
    
    public ItemReactorPlating(final ItemName name, final int maxheatadd, final float effectmodifier) {
        super(name);
        this.maxHeatAdd = maxheatadd;
        this.effectModifier = effectmodifier;
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
        if (heatrun) {
            reactor.setMaxHeat(reactor.getMaxHeat() + this.maxHeatAdd);
            reactor.setHeatEffectModifier(reactor.getHeatEffectModifier() * this.effectModifier);
        }
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        if (this.effectModifier >= 1.0f) {
            return 0.0f;
        }
        return this.effectModifier;
    }
}
