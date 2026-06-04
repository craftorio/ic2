// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;

public class ItemReactorVent extends ItemReactorHeatStorage
{
    public final int selfVent;
    public final int reactorVent;
    
    public ItemReactorVent(final ItemName name, final int heatStorage, final int selfvent, final int reactorvent) {
        super(name, heatStorage);
        this.selfVent = selfvent;
        this.reactorVent = reactorvent;
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
        if (heatrun) {
            if (this.reactorVent > 0) {
                int reactorDrain;
                int rheat = reactorDrain = reactor.getHeat();
                if (reactorDrain > this.reactorVent) {
                    reactorDrain = this.reactorVent;
                }
                rheat -= reactorDrain;
                if ((reactorDrain = this.alterHeat(stack, reactor, x, y, reactorDrain)) > 0) {
                    return;
                }
                reactor.setHeat(rheat);
            }
            final int self = this.alterHeat(stack, reactor, x, y, -this.selfVent);
            if (self <= 0) {
                reactor.addEmitHeat(self + this.selfVent);
            }
        }
    }
}
