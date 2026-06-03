package ic2.core.recipe.dynamic;

import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;

public class RecipeOutputFluidStack extends RecipeOutputIngredient<FluidStack> {
  public static RecipeOutputFluidStack of(FluidStack ingredient) {
    return new RecipeOutputFluidStack(ingredient);
  }
  
  protected RecipeOutputFluidStack(FluidStack ingredient) {
    super(ingredient);
  }
  
  public RecipeOutputIngredient<FluidStack> copy() {
    return of(this.ingredient.copy());
  }
  
  public boolean isEmpty() {
    return (this.ingredient.amount <= 0);
  }
  
  public boolean matches(Object other) {
    if (!(other instanceof FluidStack))
      return false; 
    return this.ingredient.isFluidStackIdentical((FluidStack)other);
  }
  
  public boolean matchesStrict(Object other) {
    return matches(other);
  }
  
  public String toStringSafe() {
    return LiquidUtil.toStringSafe(this.ingredient);
  }
}
