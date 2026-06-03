package ic2.core.block.machine;

import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;

public class FillFluidContainerRecipeManager implements IFillFluidContainerRecipeManager {
  public boolean addRecipe(Void input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
    return false;
  }
  
  public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(IFillFluidContainerRecipeManager.Input input, boolean acceptTest) {
    return apply(input, FluidContainerOutputMode.AnyToOutput, acceptTest);
  }
  
  public MachineRecipeResult<Void, Collection<ItemStack>, IFillFluidContainerRecipeManager.Input> apply(IFillFluidContainerRecipeManager.Input input, FluidContainerOutputMode outputMode, boolean acceptTest) {
    if (StackUtil.isEmpty(input.container) || input.fluid == null) {
      if (!acceptTest)
        return null; 
      if (StackUtil.isEmpty(input.container) && input.fluid == null)
        return null; 
      if (StackUtil.isEmpty(input.container) || LiquidUtil.isFillableFluidContainer(input.container))
        return (new MachineRecipe(null, Collections.emptyList())).getResult(input); 
      return null;
    } 
    if (input.fluid.amount <= 0)
      return null; 
    LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(input.container, input.fluid, outputMode);
    if (result == null)
      return null; 
    Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.<ItemStack>emptyList() : Collections.<ItemStack>singletonList(result.extraOutput);
    FluidStack changedFluid = (result.fluidChange.amount >= input.fluid.amount) ? null : new FluidStack(input.fluid, input.fluid.amount - result.fluidChange.amount);
    return (new MachineRecipe(null, output)).getResult(new IFillFluidContainerRecipeManager.Input(result.inPlaceOutput, changedFluid));
  }
  
  public Iterable<? extends MachineRecipe<Void, Collection<ItemStack>>> getRecipes() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isIterable() {
    return false;
  }
}
