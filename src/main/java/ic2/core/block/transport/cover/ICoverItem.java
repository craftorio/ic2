package ic2.core.block.transport.cover;

import java.util.Collection;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;

public interface ICoverItem {
   boolean isSuitableFor(ItemStack var1, Set<CoverProperty> var2);

   boolean onTick(ItemStack var1, ICoverHolder var2);

   boolean allowsInput(ItemStack var1);

   boolean allowsInput(FluidStack var1);

   boolean allowsOutput(ItemStack var1);

   boolean allowsOutput(FluidStack var1);

   Collection<? extends Capability<?>> getProvidedCapabilities();
}
