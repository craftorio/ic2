// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraftforge.fluids.FluidStack;
import java.util.Collection;
import ic2.api.util.FluidContainerOutputMode;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.item.ItemStack;

public interface IEmptyFluidContainerRecipeManager extends IMachineRecipeManager<Void, Output, ItemStack>
{
    MachineRecipeResult<Void, Output, ItemStack> apply(final ItemStack p0, final Fluid p1, final FluidContainerOutputMode p2, final boolean p3);
    
    public static class Output
    {
        public final Collection<ItemStack> container;
        public final FluidStack fluid;
        
        public Output(final Collection<ItemStack> container, final FluidStack fluid) {
            this.container = container;
            this.fluid = fluid;
        }
    }
}
