package ic2.core.recipe;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.RecipeOutput;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BasicMachineRecipeManager extends MachineRecipeHelper<IRecipeInput, Collection<ItemStack>> implements IBasicMachineRecipeManager {
  protected IRecipeInput getForInput(IRecipeInput input) {
    return input;
  }
  
  protected boolean consumeContainer(ItemStack input, ItemStack inContainer, MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe) {
    for (ItemStack output : recipe.getOutput()) {
      if (StackUtil.checkItemEqualityStrict(inContainer, output))
        return true; 
      if (output.func_77973_b().hasContainerItem(output) && StackUtil.checkItemEqualityStrict(input, output.func_77973_b().getContainerItem(output)))
        return true; 
    } 
    return false;
  }
  
  public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
    return addRecipe(input, Arrays.asList(outputs), metadata, replace);
  }
  
  public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
    if (input == null)
      throw new NullPointerException("null recipe input"); 
    if (output == null)
      throw new NullPointerException("null recipe output"); 
    if (output.isEmpty())
      throw new IllegalArgumentException("no outputs"); 
    List<ItemStack> items = new ArrayList<>(output.size());
    for (ItemStack stack : output) {
      if (StackUtil.isEmpty(stack)) {
        displayError("The output ItemStack " + StackUtil.toStringSafe(stack) + " is invalid.");
        return false;
      } 
      if (input.matches(stack))
        if (metadata == null || !metadata.func_74764_b("ignoreSameInputOutput")) {
          displayError("The output ItemStack " + stack.toString() + " is the same as the recipe input " + input + ".");
          return false;
        }  
      items.add(stack.func_77946_l());
    } 
    label31: for (ItemStack is : input.getInputs()) {
      MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe = getRecipe(is);
      if (machineRecipe != null) {
        if (replace)
          while (true) {
            this.recipes.remove(input);
            removeCachedRecipes(input);
            machineRecipe = getRecipe(is);
            if (machineRecipe == null)
              continue label31; 
          }  
        IC2.log.debug(LogCategory.Recipe, "Skipping %s => %s due to duplicate recipe for %s (%s => %s)", new Object[] { input, output, is, machineRecipe.getInput(), machineRecipe.getOutput() });
        return false;
      } 
    } 
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = new MachineRecipe(input, items, metadata);
    this.recipes.put(input, recipe);
    addToCache(recipe);
    return true;
  }
  
  public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = getRecipe(input);
    if (recipe == null)
      return null; 
    if (StackUtil.getSize(input) >= ((IRecipeInput)recipe.getInput()).getAmount() && (
      !input.func_77973_b().hasContainerItem(input) || StackUtil.getSize(input) == ((IRecipeInput)recipe.getInput()).getAmount())) {
      if (adjustInput) {
        if (input.func_77973_b().hasContainerItem(input))
          throw new UnsupportedOperationException("can't adjust input item, use apply() instead"); 
        input.func_190918_g(((IRecipeInput)recipe.getInput()).getAmount());
      } 
      return new RecipeOutput(recipe.getMetaData(), new ArrayList((Collection)recipe.getOutput()));
    } 
    return null;
  }
  
  public void removeRecipe(ItemStack input, Collection<ItemStack> output) {
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = getRecipe(input);
    if (recipe != null && checkListEquality((Collection<ItemStack>)recipe.getOutput(), output)) {
      this.recipes.remove(recipe.getInput());
      removeCachedRecipes((IRecipeInput)recipe.getInput());
    } 
  }
  
  private static boolean checkListEquality(Collection<ItemStack> a, Collection<ItemStack> b) {
    if (a.size() != b.size())
      return false; 
    ListIterator<ItemStack> itB = (new ArrayList<>(b)).listIterator();
    for (ItemStack stack : a) {
      while (itB.hasNext()) {
        if (StackUtil.checkItemEqualityStrict(stack, itB.next())) {
          itB.remove();
          for (; itB.hasPrevious(); itB.previous());
        } 
      } 
      return false;
    } 
    return true;
  }
  
  private void displayError(String msg) {
    if (MainConfig.ignoreInvalidRecipes) {
      IC2.log.warn(LogCategory.Recipe, msg);
    } else {
      throw new RuntimeException(msg);
    } 
  }
}
