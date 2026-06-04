package ic2.core.recipe;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class BasicMachineRecipeManagerTest implements IBasicMachineRecipeManager {
  public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
    if (replace) {
      this.recipes.add(0, new MachineRecipe(input, output, metadata));
    } else if (getCollidingRecipe(input) == null) {
      this.recipes.add(new MachineRecipe(input, output, metadata));
    } else {
      return false;
    } 
    return true;
  }
  
  public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
    return addRecipe(input, Arrays.asList(outputs), metadata, replace);
  }
  
  public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = getRecipe(input, true);
    if (recipe == null)
      return null; 
    if (adjustInput) {
      if (input.getItem().hasContainerItem(input))
        throw new UnsupportedOperationException("can't adjust input item, use apply() instead"); 
      input.func_190918_g(((IRecipeInput)recipe.getInput()).getAmount());
    } 
    return new RecipeOutput(recipe.getMetaData(), new ArrayList((Collection)recipe.getOutput()));
  }
  
  public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
    if (StackUtil.isEmpty(input))
      return null; 
    MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = getRecipe(input, true);
    if (recipe == null)
      return null; 
    ItemStack adjustedInput;
    if (input.getItem().hasContainerItem(input) && 
      !StackUtil.isEmpty(adjustedInput = input.getItem().getContainerItem(input))) {
      if (StackUtil.getSize(input) != ((IRecipeInput)recipe.getInput()).getAmount())
        return null; 
      adjustedInput = StackUtil.copy(input);
    } else {
      adjustedInput = StackUtil.copyWithSize(input, StackUtil.getSize(input) - ((IRecipeInput)recipe.getInput()).getAmount());
    } 
    return recipe.getResult(adjustedInput);
  }
  
  private MachineRecipe<IRecipeInput, Collection<ItemStack>> getCollidingRecipe(IRecipeInput input) {
    for (ItemStack itemStackIn : input.getInputs()) {
      MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = getRecipe(itemStackIn, false);
      if (recipe != null)
        return recipe; 
    } 
    return null;
  }
  
  private MachineRecipe<IRecipeInput, Collection<ItemStack>> getRecipe(ItemStack stack, boolean checkAmount) {
    for (MachineRecipe<IRecipeInput, Collection<ItemStack>> container : this.recipes) {
      if (((IRecipeInput)container.getInput()).matches(stack)) {
        if (!checkAmount)
          return container; 
        if (StackUtil.getSize(stack) >= ((IRecipeInput)container.getInput()).getAmount() && (
          !stack.getItem().hasContainerItem(stack) || StackUtil.getSize(stack) == ((IRecipeInput)container.getInput()).getAmount()))
          return container; 
      } 
    } 
    return null;
  }
  
  public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
    return this.recipes;
  }
  
  public boolean isIterable() {
    return true;
  }
  
  private final List<MachineRecipe<IRecipeInput, Collection<ItemStack>>> recipes = new ArrayList<>();
}
