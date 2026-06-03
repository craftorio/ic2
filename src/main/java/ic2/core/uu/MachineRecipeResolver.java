package ic2.core.uu;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class MachineRecipeResolver implements IRecipeResolver {
  private static final double transformCost = 14.0D;
  
  private final IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager;
  
  public MachineRecipeResolver(IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ?> manager) {
    this.manager = manager;
  }
  
  public List<RecipeTransformation> getTransformations() {
    if (!this.manager.isIterable())
      return Collections.emptyList(); 
    List<RecipeTransformation> ret = new ArrayList<>();
    for (MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe : (Iterable<MachineRecipe<IRecipeInput, Collection<ItemStack>>>)this.manager.getRecipes()) {
      try {
        List<List<LeanItemStack>> inputs = RecipeUtil.convertIngredients(((IRecipeInput)recipe.getInput()).getInputs());
        List<LeanItemStack> outputs = RecipeUtil.convertOutputs((Collection<ItemStack>)recipe.getOutput());
        ret.add(new RecipeTransformation(14.0D, inputs, outputs));
      } catch (IllegalArgumentException e) {
        IC2.log.warn(LogCategory.Uu, e, "invalid recipe");
      } 
    } 
    return ret;
  }
}
