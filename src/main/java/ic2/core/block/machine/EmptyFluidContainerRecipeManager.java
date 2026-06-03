package ic2.core.block.machine;

import ic2.api.recipe.IEmptyFluidContainerRecipeManager;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;

public class EmptyFluidContainerRecipeManager implements IEmptyFluidContainerRecipeManager {
  public boolean addRecipe(Void input, IEmptyFluidContainerRecipeManager.Output output, NBTTagCompound metadata, boolean replace) {
    return false;
  }
  
  public MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack input, boolean acceptTest) {
    return apply(input, null, FluidContainerOutputMode.AnyToOutput, acceptTest);
  }
  
  public MachineRecipeResult<Void, IEmptyFluidContainerRecipeManager.Output, ItemStack> apply(ItemStack input, Fluid requiredFluid, FluidContainerOutputMode outputMode, boolean acceptTest) {
    if (StackUtil.isEmpty(input))
      return null; 
    LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(input, requiredFluid, 2147483647, outputMode);
    if (result == null)
      return null; 
    Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.<ItemStack>emptyList() : Collections.<ItemStack>singletonList(result.extraOutput);
    return (new MachineRecipe(null, new IEmptyFluidContainerRecipeManager.Output(output, result.fluidChange))).getResult(result.inPlaceOutput);
  }
  
  public Iterable<? extends MachineRecipe<Void, IEmptyFluidContainerRecipeManager.Output>> getRecipes() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isIterable() {
    return false;
  }
}
