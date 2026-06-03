package ic2.core.block.machine;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CannerBottleRecipeManager implements ICannerBottleRecipeManager {
  public boolean addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output, boolean replace) {
    return addRecipe(new ICannerBottleRecipeManager.Input(container, fill), output, (NBTTagCompound)null, replace);
  }
  
  @Deprecated
  public void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output) {
    if (!addRecipe(container, fill, output, false))
      throw new IllegalStateException("ambiguous canner bottle recipe: " + container + " + " + fill + " -> " + output); 
  }
  
  public boolean addRecipe(ICannerBottleRecipeManager.Input input, ItemStack output, NBTTagCompound metadata, boolean replace) {
    for (Iterator<MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> it = this.recipes.iterator(); it.hasNext(); ) {
      MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe = it.next();
      for (ItemStack containerStack : input.container.getInputs()) {
        for (ItemStack fillStack : input.fill.getInputs()) {
          if (((ICannerBottleRecipeManager.Input)recipe.getInput()).matches(containerStack, fillStack)) {
            if (replace) {
              it.remove();
              continue;
            } 
            IC2.log.warn(LogCategory.Recipe, "ambiguous recipe: [" + input.container.getInputs() + "+" + input.fill.getInputs() + " -> " + output + "], conflicts with [" + ((ICannerBottleRecipeManager.Input)recipe
                .getInput()).container.getInputs() + "+" + ((ICannerBottleRecipeManager.Input)recipe.getInput()).fill.getInputs() + " -> " + recipe.getOutput() + "]");
            return false;
          } 
        } 
      } 
    } 
    this.recipes.add(new MachineRecipe(input, output));
    return true;
  }
  
  public RecipeOutput getOutputFor(ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptTest) {
    if (acceptTest) {
      if (StackUtil.isEmpty(container) && StackUtil.isEmpty(fill))
        return null; 
    } else if (StackUtil.isEmpty(container) || StackUtil.isEmpty(fill)) {
      return null;
    } 
    for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : this.recipes) {
      ICannerBottleRecipeManager.Input recipeInput = (ICannerBottleRecipeManager.Input)recipe.getInput();
      if (acceptTest && StackUtil.isEmpty(container)) {
        if (recipeInput.fill.matches(fill))
          return new RecipeOutput(null, new ItemStack[] { (ItemStack)recipe.getOutput() }); 
        continue;
      } 
      if (acceptTest && StackUtil.isEmpty(fill)) {
        if (recipeInput.container.matches(container))
          return new RecipeOutput(null, new ItemStack[] { (ItemStack)recipe.getOutput() }); 
        continue;
      } 
      if (recipeInput.matches(container, fill)) {
        if (acceptTest || (!StackUtil.isEmpty(container) && StackUtil.getSize(container) >= recipeInput.container.getAmount() && StackUtil.getSize(fill) >= recipeInput.fill.getAmount())) {
          if (adjustInput) {
            if (!StackUtil.isEmpty(container))
              container.func_190918_g(recipeInput.container.getAmount()); 
            fill.func_190918_g(recipeInput.fill.getAmount());
          } 
          new RecipeOutput(null, new ItemStack[] { (ItemStack)recipe.getOutput() });
        } 
        break;
      } 
    } 
    return null;
  }
  
  public MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> apply(ICannerBottleRecipeManager.RawInput input, boolean acceptTest) {
    boolean emptyContainer = StackUtil.isEmpty(input.container);
    boolean emptyFill = StackUtil.isEmpty(input.fill);
    if (!acceptTest && (emptyContainer || emptyFill))
      return null; 
    if (acceptTest && emptyContainer && emptyFill)
      return null; 
    for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : this.recipes) {
      if ((emptyContainer || (((ICannerBottleRecipeManager.Input)recipe.getInput()).container.matches(input.container) && ((ICannerBottleRecipeManager.Input)recipe.getInput()).container.getAmount() <= StackUtil.getSize(input.container))) && (emptyFill || (((ICannerBottleRecipeManager.Input)recipe
        .getInput()).fill.matches(input.fill) && ((ICannerBottleRecipeManager.Input)recipe.getInput()).fill.getAmount() <= StackUtil.getSize(input.fill))))
        return recipe.getResult(new ICannerBottleRecipeManager.RawInput(emptyContainer ? StackUtil.emptyStack : StackUtil.copyShrunk(input.container, ((ICannerBottleRecipeManager.Input)recipe.getInput()).container.getAmount()), emptyFill ? StackUtil.emptyStack : 
              StackUtil.copyShrunk(input.fill, ((ICannerBottleRecipeManager.Input)recipe.getInput()).fill.getAmount()))); 
    } 
    return null;
  }
  
  public Iterable<? extends MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> getRecipes() {
    return this.recipes;
  }
  
  public boolean isIterable() {
    return true;
  }
  
  private final List<MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> recipes = new ArrayList<>();
}
