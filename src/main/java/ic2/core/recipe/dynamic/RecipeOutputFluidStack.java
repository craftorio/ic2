// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;

public class RecipeOutputFluidStack extends RecipeOutputIngredient<FluidStack>
{
    public static RecipeOutputFluidStack of(final FluidStack ingredient) {
        return new RecipeOutputFluidStack(ingredient);
    }
    
    protected RecipeOutputFluidStack(final FluidStack ingredient) {
        super(ingredient);
    }
    
    @Override
    public RecipeOutputIngredient<FluidStack> copy() {
        return of(((FluidStack)this.ingredient).copy());
    }
    
    @Override
    public boolean isEmpty() {
        return ((FluidStack)this.ingredient).amount <= 0;
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
