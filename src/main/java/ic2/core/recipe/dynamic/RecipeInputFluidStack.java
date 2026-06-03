package ic2.core.recipe.dynamic;

import ic2.core.util.LiquidUtil;
import net.minecraftforge.fluids.FluidStack;

public class RecipeInputFluidStack extends RecipeInputIngredient<FluidStack> {
  public static RecipeInputFluidStack of(FluidStack ingredient) {
    return new RecipeInputFluidStack(ingredient);
  }
  
  protected RecipeInputFluidStack(FluidStack ingredient) {
    super(ingredient);
  }
  
  public Object getUnspecific() {
    return this.ingredient.getFluid();
  }
  
  public RecipeInputIngredient<FluidStack> copy() {
    return of(this.ingredient.copy());
  }
  
  public boolean isEmpty() {
    return (this.ingredient.amount <= 0);
  }
  
  public int getCount() {
    return this.ingredient.amount;
  }
  
  public void shrink(int amount) {
    this.ingredient.amount -= amount;
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
