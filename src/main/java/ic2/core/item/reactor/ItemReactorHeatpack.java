// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactorComponent;
import ic2.core.util.StackUtil;
import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.core.profile.NotExperimental;

@NotExperimental
public class ItemReactorHeatpack extends AbstractReactorComponent
{
    protected final int maxPer;
    protected final int heatPer;
    
    public ItemReactorHeatpack(final int maxPer, final int heatPer) {
        super(ItemName.heatpack);
        this.maxPer = maxPer;
        this.heatPer = heatPer;
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
        if (heatrun) {
            final int size = StackUtil.getSize(stack);
            this.heat(reactor, size, x + 1, y);
            this.heat(reactor, size, x - 1, y);
            this.heat(reactor, size, x, y + 1);
            this.heat(reactor, size, x, y - 1);
        }
    }
    
    private void heat(final IReactor reactor, final int size, final int x, final int y) {
        final int want = this.maxPer * size;
        if (reactor.getHeat() >= want) {
            return;
        }
        final ItemStack stack = reactor.getItemAt(x, y);
        if (!StackUtil.isEmpty(stack) && stack.getItem() instanceof IReactorComponent) {
            final IReactorComponent comp = (IReactorComponent)stack.getItem();
            if (comp.canStoreHeat(stack, reactor, x, y)) {
                int add = this.heatPer * size;
                final int curr = comp.getCurrentHeat(stack, reactor, x, y);
                if (add > want - curr) {
                    add = want - curr;
                }
                if (add > 0) {
                    comp.alterHeat(stack, reactor, x, y, add);
                }
            }
        }
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return StackUtil.getSize(stack) / 10.0f;
    }
}
