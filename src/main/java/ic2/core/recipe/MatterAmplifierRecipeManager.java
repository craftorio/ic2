package ic2.core.recipe;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MatterAmplifierRecipeManager implements IMachineRecipeManager<IRecipeInput, Integer, ItemStack> {
  public boolean addRecipe(IRecipeInput input, Integer output, NBTTagCompound metadata, boolean replace) {
    if (output.intValue() <= 0)
      throw new IllegalArgumentException("non-positive amplification"); 
    for (ItemStack stack : input.getInputs()) {
      MachineRecipe<IRecipeInput, Integer> recipe = getRecipe(stack, true);
      if (recipe != null) {
        if (!replace)
          return false; 
        this.recipes.remove(recipe);
      } 
    } 
    this.recipes.add(new MachineRecipe(input, output));
    return true;
  }
  
  public MachineRecipeResult<IRecipeInput, Integer, ItemStack> apply(ItemStack input, boolean acceptTest) {
    MachineRecipe<IRecipeInput, Integer> recipe = getRecipe(input, acceptTest);
    if (recipe == null)
      return null; 
    return recipe.getResult(StackUtil.copyShrunk(input, ((IRecipeInput)recipe.getInput()).getAmount()));
  }
  
  private MachineRecipe<IRecipeInput, Integer> getRecipe(ItemStack stack, boolean acceptTest) {
    for (MachineRecipe<IRecipeInput, Integer> recipe : this.recipes) {
      if (((IRecipeInput)recipe.getInput()).matches(stack) && (acceptTest || ((IRecipeInput)recipe.getInput()).getAmount() <= StackUtil.getSize(stack)))
        return recipe; 
    } 
    return null;
  }
  
  public Iterable<? extends MachineRecipe<IRecipeInput, Integer>> getRecipes() {
    return this.recipes;
  }
  
  public boolean isIterable() {
    return true;
  }
  
  private final List<MachineRecipe<IRecipeInput, Integer>> recipes = new ArrayList<>();
}
