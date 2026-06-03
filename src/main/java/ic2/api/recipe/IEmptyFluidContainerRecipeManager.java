package ic2.api.recipe;

import ic2.api.util.FluidContainerOutputMode;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> {
  MachineRecipeResult<Void, Output, ItemStack> apply(ItemStack paramItemStack, Fluid paramFluid, FluidContainerOutputMode paramFluidContainerOutputMode, boolean paramBoolean);
  
  public static class Output {
    public final Collection<ItemStack> container;
    
    public final FluidStack fluid;
    
    public Output(Collection<ItemStack> container, FluidStack fluid) {
      this.container = container;
      this.fluid = fluid;
    }
  }
}
