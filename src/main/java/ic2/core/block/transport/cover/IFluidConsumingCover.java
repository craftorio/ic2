package ic2.core.block.transport.cover;

import java.util.Collection;
import java.util.Collections;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;

public interface IFluidConsumingCover extends ICoverItem {
   @Override
   default Collection<? extends Capability<?>> getProvidedCapabilities() {
      return Collections.singleton(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
   }
}
