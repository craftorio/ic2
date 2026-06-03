package ic2.core.block.machine;

import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class CannerEnrichRecipeManager implements ICannerEnrichRecipeManager {
  public boolean addRecipe(ICannerEnrichRecipeManager.Input input, FluidStack output, NBTTagCompound metadata, boolean replace) {
    if (input.fluid == null)
      throw new NullPointerException("The fluid recipe input is null."); 
    if (input.additive == null)
      throw new NullPointerException("The additive recipe input is null."); 
    if (output == null)
      throw new NullPointerException("The recipe output is null."); 
    if (!LiquidUtil.check(input.fluid))
      throw new IllegalArgumentException("The fluid recipe input is invalid."); 
    if (!LiquidUtil.check(output))
      throw new IllegalArgumentException("The fluid recipe output is invalid."); 
    for (ItemStack stack : input.additive.getInputs()) {
      MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe = getRecipe(input.fluid, stack, true);
      if (recipe != null) {
        if (!replace)
          return false; 
        this.recipes.remove(recipe);
      } 
    } 
    this.recipes.add(new MachineRecipe(input, output));
    return true;
  }
  
  public void addRecipe(FluidStack fluid, IRecipeInput additive, FluidStack output) {
    if (!addRecipe(new ICannerEnrichRecipeManager.Input(fluid, additive), output, (NBTTagCompound)null, false))
      throw new RuntimeException("ambiguous recipe: [" + fluid + "+" + additive.getInputs() + " -> " + output + "]"); 
  }
  
  public MachineRecipeResult<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput> apply(ICannerEnrichRecipeManager.RawInput input, boolean acceptTest) {
    FluidStack remainingFluid;
    MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe = getRecipe(input.fluid, input.additive, acceptTest);
    if (recipe == null)
      return null; 
    if (input.fluid == null) {
      remainingFluid = null;
    } else {
      remainingFluid = input.fluid.copy();
      remainingFluid.amount -= ((ICannerEnrichRecipeManager.Input)recipe.getInput()).fluid.amount;
      if (remainingFluid.amount <= 0)
        remainingFluid = null; 
    } 
    return recipe.getResult(new ICannerEnrichRecipeManager.RawInput(remainingFluid, StackUtil.copyShrunk(input.additive, ((ICannerEnrichRecipeManager.Input)recipe.getInput()).additive.getAmount())));
  }
  
  private MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> getRecipe(FluidStack fluid, ItemStack additive, boolean acceptTest) {
    if (!acceptTest && (fluid == null || StackUtil.isEmpty(additive)))
      return null; 
    for (MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe : this.recipes) {
      if ((fluid == null || (fluid.isFluidEqual(((ICannerEnrichRecipeManager.Input)recipe.getInput()).fluid) && (acceptTest || ((ICannerEnrichRecipeManager.Input)recipe.getInput()).fluid.amount <= fluid.amount))) && (additive == null || (((ICannerEnrichRecipeManager.Input)recipe
        .getInput()).additive.matches(additive) && (acceptTest || ((ICannerEnrichRecipeManager.Input)recipe.getInput()).additive.getAmount() <= StackUtil.getSize(additive)))))
        return recipe; 
    } 
    return null;
  }
  
  public RecipeOutput getOutputFor(FluidStack fluid, ItemStack additive, boolean adjustInput, boolean acceptTest) {
    MachineRecipeResult<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput> result = apply(new ICannerEnrichRecipeManager.RawInput(fluid, additive), acceptTest);
    if (result == null)
      return null; 
    if (adjustInput) {
      fluid.amount = (((ICannerEnrichRecipeManager.RawInput)result.getAdjustedInput()).fluid == null) ? 0 : ((ICannerEnrichRecipeManager.RawInput)result.getAdjustedInput()).fluid.amount;
      additive.func_190920_e(StackUtil.isEmpty(((ICannerEnrichRecipeManager.RawInput)result.getAdjustedInput()).additive) ? 0 : StackUtil.getSize(((ICannerEnrichRecipeManager.RawInput)result.getAdjustedInput()).additive));
    } 
    NBTTagCompound output = new NBTTagCompound();
    ((FluidStack)result.getOutput()).writeToNBT(output);
    return new RecipeOutput(output, new ItemStack[0]);
  }
  
  public Iterable<? extends MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack>> getRecipes() {
    return this.recipes;
  }
  
  public boolean isIterable() {
    return true;
  }
  
  private final List<MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack>> recipes = new ArrayList<>();
}
