// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.reactor.IReactorComponent;
import ic2.core.item.ItemIC2;

public abstract class AbstractReactorComponent extends ItemIC2 implements IReactorComponent
{
    protected AbstractReactorComponent(final ItemName name) {
        super(name);
    }
    
    @Override
    public void processChamber(final ItemStack stack, final IReactor reactor, final int x, final int y, final boolean heatrun) {
    }
    
    @Override
    public boolean acceptUraniumPulse(final ItemStack stack, final IReactor reactor, final ItemStack pulsingStack, final int youX, final int youY, final int pulseX, final int pulseY, final boolean heatrun) {
        return false;
    }
    
    @Override
    public boolean canStoreHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return false;
    }
    
    @Override
    public int getMaxHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return 0;
    }
    
    @Override
    public int getCurrentHeat(final ItemStack stack, final IReactor reactor, final int x, final int y) {
        return 0;
    }
    
    @Override
    public int alterHeat(final ItemStack stack, final IReactor reactor, final int x, final int y, final int heat) {
        return heat;
    }
    
    @Override
    public float influenceExplosion(final ItemStack stack, final IReactor reactor) {
        return 0.0f;
    }
    
    public boolean canBePlacedIn(final ItemStack stack, final IReactor reactor) {
        return true;
    }
}
