// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.transport.cover;

import java.util.Collections;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.common.capabilities.Capability;
import java.util.Collection;

public interface IFluidConsumingCover extends ICoverItem
{
    default Collection<? extends Capability<?>> getProvidedCapabilities() {
        return (Collection<? extends Capability<?>>)Collections.singleton(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
    }
}
