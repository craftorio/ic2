// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public interface ICannerEnrichRecipeManager extends IMachineRecipeManager<Input, FluidStack, RawInput>
{
    @Deprecated
    void addRecipe(final FluidStack p0, final IRecipeInput p1, final FluidStack p2);
    
    @Deprecated
    RecipeOutput getOutputFor(final FluidStack p0, final ItemStack p1, final boolean p2, final boolean p3);
    
    public static class Input
    {
        public final FluidStack fluid;
        public final IRecipeInput additive;
        
        public Input(final FluidStack fluid, final IRecipeInput additive) {
            this.fluid = fluid;
            this.additive = additive;
        }
        
        public boolean matches(final FluidStack fluid, final ItemStack additive) {
            return this.fluid.isFluidEqual(fluid) && this.additive.matches(additive);
        }
    }
    
    public static class RawInput
    {
        public final FluidStack fluid;
        public final ItemStack additive;
        
        public RawInput(final FluidStack fluid, final ItemStack additive) {
            this.fluid = fluid;
            this.additive = additive;
        }
    }
}
