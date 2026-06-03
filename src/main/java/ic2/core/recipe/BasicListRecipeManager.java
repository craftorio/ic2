package ic2.core.recipe;

import ic2.api.recipe.IListRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BasicListRecipeManager extends MachineRecipeHelper<IRecipeInput, Object> implements IListRecipeManager {
  public void add(IRecipeInput input) {
    if (input == null)
      throw new NullPointerException("Input must not be null."); 
    addRecipe(input, dummyOutput, (NBTTagCompound)null, false);
  }
  
  public boolean contains(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return false; 
    return (getRecipe(stack) != null);
  }
  
  public boolean isEmpty() {
    return this.recipes.isEmpty();
  }
  
  public List<IRecipeInput> getInputs() {
    return new ArrayList<>(this.recipes.keySet());
  }
  
  public Iterator<IRecipeInput> iterator() {
    return this.recipes.keySet().iterator();
  }
  
  public boolean addRecipe(IRecipeInput input, Object output, NBTTagCompound metadata, boolean replace) {
    label14: for (ItemStack is : input.getInputs()) {
      MachineRecipe<IRecipeInput, Object> machineRecipe = getRecipe(is);
      if (machineRecipe != null) {
        if (replace)
          while (true) {
            this.recipes.remove(input);
            removeCachedRecipes(input);
            machineRecipe = getRecipe(is);
            if (machineRecipe == null)
              continue label14; 
          }  
        IC2.log.debug(LogCategory.Recipe, "Skipping %s due to duplicate recipe for %s (%s)", new Object[] { input, is, machineRecipe.getInput() });
        return false;
      } 
    } 
    MachineRecipe<IRecipeInput, Object> recipe = new MachineRecipe(input, output, metadata);
    this.recipes.put(input, recipe);
    addToCache(recipe);
    return false;
  }
  
  protected IRecipeInput getForInput(IRecipeInput input) {
    return input;
  }
  
  protected boolean consumeContainer(ItemStack input, ItemStack container, MachineRecipe<IRecipeInput, Object> recipe) {
    return true;
  }
  
  private static final Object dummyOutput = new Object();
}
