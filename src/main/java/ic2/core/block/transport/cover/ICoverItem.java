// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.cover;

import net.minecraftforge.common.capabilities.Capability;
import java.util.Collection;
import net.minecraftforge.fluids.FluidStack;
import java.util.Set;
import net.minecraft.item.ItemStack;

public interface ICoverItem
{
    boolean isSuitableFor(final ItemStack p0, final Set<CoverProperty> p1);
    
    boolean onTick(final ItemStack p0, final ICoverHolder p1);
    
    boolean allowsInput(final ItemStack p0);
    
    boolean allowsInput(final FluidStack p0);
    
    boolean allowsOutput(final ItemStack p0);
    
    boolean allowsOutput(final FluidStack p0);
    
    Collection<? extends Capability<?>> getProvidedCapabilities();
}
