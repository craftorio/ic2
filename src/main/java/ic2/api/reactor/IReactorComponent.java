// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.reactor;

import net.minecraft.item.ItemStack;

public interface IReactorComponent extends IBaseReactorComponent
{
    void processChamber(final ItemStack p0, final IReactor p1, final int p2, final int p3, final boolean p4);
    
    boolean acceptUraniumPulse(final ItemStack p0, final IReactor p1, final ItemStack p2, final int p3, final int p4, final int p5, final int p6, final boolean p7);
    
    boolean canStoreHeat(final ItemStack p0, final IReactor p1, final int p2, final int p3);
    
    int getMaxHeat(final ItemStack p0, final IReactor p1, final int p2, final int p3);
    
    int getCurrentHeat(final ItemStack p0, final IReactor p1, final int p2, final int p3);
    
    int alterHeat(final ItemStack p0, final IReactor p1, final int p2, final int p3, final int p4);
    
    float influenceExplosion(final ItemStack p0, final IReactor p1);
}
