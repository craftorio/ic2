package ic2.core.block.transport.cover;

import java.util.Collection;
import java.util.Set;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;

public interface ICoverItem {
  boolean isSuitableFor(ItemStack paramItemStack, Set<CoverProperty> paramSet);
  
  boolean onTick(ItemStack paramItemStack, ICoverHolder paramICoverHolder);
  
  boolean allowsInput(ItemStack paramItemStack);
  
  boolean allowsInput(FluidStack paramFluidStack);
  
  boolean allowsOutput(ItemStack paramItemStack);
  
  boolean allowsOutput(FluidStack paramFluidStack);
  
  Collection<? extends Capability<?>> getProvidedCapabilities();
}
