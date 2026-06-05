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
   private final List<MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack>> recipes = new ArrayList<>();

   public boolean addRecipe(ICannerEnrichRecipeManager.Input input, FluidStack output, NBTTagCompound metadata, boolean replace) {
      if (input.fluid == null) {
         throw new NullPointerException("The fluid recipe input is null.");
      }

      if (input.additive == null) {
         throw new NullPointerException("The additive recipe input is null.");
      }

      if (output == null) {
         throw new NullPointerException("The recipe output is null.");
      }

      if (!LiquidUtil.check(input.fluid)) {
         throw new IllegalArgumentException("The fluid recipe input is invalid.");
      }

      if (!LiquidUtil.check(output)) {
         throw new IllegalArgumentException("The fluid recipe output is invalid.");
      }

      for (ItemStack stack : input.additive.getInputs()) {
         MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe = this.getRecipe(input.fluid, stack, true);
         if (recipe != null) {
            if (!replace) {
               return false;
            }

            this.recipes.remove(recipe);
         }
      }

      this.recipes.add(new MachineRecipe<>(input, output));
      return true;
   }

   @Override
   public void addRecipe(FluidStack fluid, IRecipeInput additive, FluidStack output) {
      if (!this.addRecipe(new ICannerEnrichRecipeManager.Input(fluid, additive), output, null, false)) {
         throw new RuntimeException("ambiguous recipe: [" + fluid + "+" + additive.getInputs() + " -> " + output + "]");
      }
   }

   public MachineRecipeResult<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput> apply(
      ICannerEnrichRecipeManager.RawInput input, boolean acceptTest
   ) {
      MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe = this.getRecipe(input.fluid, input.additive, acceptTest);
      if (recipe == null) {
         return null;
      }

      FluidStack remainingFluid;
      if (input.fluid == null) {
         remainingFluid = null;
      } else {
         remainingFluid = input.fluid.copy();
         remainingFluid.amount = remainingFluid.amount - recipe.getInput().fluid.amount;
         if (remainingFluid.amount <= 0) {
            remainingFluid = null;
         }
      }

      return recipe.getResult(
         new ICannerEnrichRecipeManager.RawInput(remainingFluid, StackUtil.copyShrunk(input.additive, recipe.getInput().additive.getAmount()))
      );
   }

   private MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> getRecipe(FluidStack fluid, ItemStack additive, boolean acceptTest) {
      if (acceptTest || fluid != null && !StackUtil.isEmpty(additive)) {
         for (MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack> recipe : this.recipes) {
            if ((fluid == null || fluid.isFluidEqual(recipe.getInput().fluid) && (acceptTest || recipe.getInput().fluid.amount <= fluid.amount))
               && (
                  additive == null
                     || recipe.getInput().additive.matches(additive) && (acceptTest || recipe.getInput().additive.getAmount() <= StackUtil.getSize(additive))
               )) {
               return recipe;
            }
         }

         return null;
      } else {
         return null;
      }
   }

   @Override
   public RecipeOutput getOutputFor(FluidStack fluid, ItemStack additive, boolean adjustInput, boolean acceptTest) {
      MachineRecipeResult<ICannerEnrichRecipeManager.Input, FluidStack, ICannerEnrichRecipeManager.RawInput> result = this.apply(
         new ICannerEnrichRecipeManager.RawInput(fluid, additive), acceptTest
      );
      if (result == null) {
         return null;
      }

      if (adjustInput) {
         fluid.amount = result.getAdjustedInput().fluid == null ? 0 : result.getAdjustedInput().fluid.amount;
         additive.setCount(StackUtil.isEmpty(result.getAdjustedInput().additive) ? 0 : StackUtil.getSize(result.getAdjustedInput().additive));
      }

      NBTTagCompound output = new NBTTagCompound();
      result.getOutput().writeToNBT(output);
      return new RecipeOutput(output);
   }

   @Override
   public Iterable<? extends MachineRecipe<ICannerEnrichRecipeManager.Input, FluidStack>> getRecipes() {
      return this.recipes;
   }

   @Override
   public boolean isIterable() {
      return true;
   }
}
