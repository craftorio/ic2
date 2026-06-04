// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;

public class RecipeInputFluidStack extends RecipeInputIngredient<FluidStack>
{
    public static RecipeInputFluidStack of(final FluidStack ingredient) {
        return new RecipeInputFluidStack(ingredient);
    }
    
    protected RecipeInputFluidStack(final FluidStack ingredient) {
        super(ingredient);
    }
    
    @Override
    public Object getUnspecific() {
        return ((FluidStack)this.ingredient).getFluid();
    }
    
    @Override
    public RecipeInputIngredient<FluidStack> copy() {
        return of(((FluidStack)this.ingredient).copy());
    }
    
    @Override
    public boolean isEmpty() {
        return ((FluidStack)this.ingredient).amount <= 0;
    }
    
    @Override
    public int getCount() {
        return ((FluidStack)this.ingredient).amount;
    }
    
    @Override
    public void shrink(final int amount) {
        final FluidStack fluidStack = (FluidStack)this.ingredient;
        fluidStack.amount -= amount;
    }
    
    @Override
    public boolean matches(final Object other) {
        return other instanceof FluidStack && ((FluidStack)this.ingredient).isFluidStackIdentical((FluidStack)other);
    }
    
    @Override
    public boolean matchesStrict(final Object other) {
        return this.matches(other);
    }
    
    @Override
    public String toStringSafe() {
        return LiquidUtil.toStringSafe((FluidStack)this.ingredient);
    }
}
