package ic2.core.recipe;

import com.google.common.collect.Iterables;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IRecipeInputFactory;
import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;

public class RecipeInputFactory implements IRecipeInputFactory {
  public IRecipeInput forStack(ItemStack stack) {
    return new RecipeInputItemStack(stack);
  }
  
  public IRecipeInput forStack(ItemStack stack, int amount) {
    return new RecipeInputItemStack(stack, amount);
  }
  
  public IRecipeInput forExactStack(ItemStack stack) {
    if (stack.getMetadata() == 32767)
      return forStack(stack); 
    return new RecipeInputItemStackExact(stack);
  }
  
  public IRecipeInput forExactStack(ItemStack stack, int amount) {
    if (stack.getMetadata() == 32767)
      return forStack(stack, amount); 
    return new RecipeInputItemStackExact(stack, amount);
  }
  
  public IRecipeInput forOreDict(String name) {
    return new RecipeInputOreDict(name);
  }
  
  public IRecipeInput forOreDict(String name, int amount) {
    return new RecipeInputOreDict(name, amount);
  }
  
  public IRecipeInput forOreDict(String name, int amount, int metaOverride) {
    return new RecipeInputOreDict(name, amount, Integer.valueOf(metaOverride));
  }
  
  public IRecipeInput forFluidContainer(Fluid fluid) {
    return new RecipeInputFluidContainer(fluid);
  }
  
  public IRecipeInput forFluidContainer(Fluid fluid, int amount) {
    return new RecipeInputFluidContainer(fluid, amount);
  }
  
  public IRecipeInput forAny(IRecipeInput... options) {
    return new RecipeInputMultiple(options);
  }
  
  public IRecipeInput forAny(Iterable<IRecipeInput> options) {
    if (options instanceof Collection)
      return new RecipeInputMultiple((IRecipeInput[])((Collection)options).toArray((Object[])new IRecipeInput[0])); 
    return new RecipeInputMultiple((IRecipeInput[])Iterables.toArray(options, IRecipeInput.class));
  }
  
  public Ingredient getIngredient(IRecipeInput input) {
    return new IngredientRecipeInput(input);
  }
  
  public IRecipeInput forIngredient(Ingredient ingredient) {
    return new RecipeInputIngredient(ingredient);
  }
}
