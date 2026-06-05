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
   private final List<MachineRecipe<IRecipeInput, Collection<ItemStack>>> recipes = new ArrayList<>();

   public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
      if (replace) {
         this.recipes.add(0, new MachineRecipe<>(input, output, metadata));
      } else {
         if (this.getCollidingRecipe(input) != null) {
            return false;
         }

         this.recipes.add(new MachineRecipe<>(input, output, metadata));
      }

      return true;
   }

   @Override
   public boolean addRecipe(IRecipeInput input, NBTTagCompound metadata, boolean replace, ItemStack... outputs) {
      return this.addRecipe(input, Arrays.asList(outputs), metadata, replace);
   }

   @Override
   public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput) {
      MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input, true);
      if (recipe == null) {
         return null;
      }

      if (adjustInput) {
         if (input.getItem().hasContainerItem(input)) {
            throw new UnsupportedOperationException("can't adjust input item, use apply() instead");
         }

         input.shrink(recipe.getInput().getAmount());
      }

      return new RecipeOutput(recipe.getMetaData(), new ArrayList<>(recipe.getOutput()));
   }

   public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
      if (StackUtil.isEmpty(input)) {
         return null;
      }

      MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input, true);
      if (recipe == null) {
         return null;
      }

      ItemStack adjustedInput;
      if (input.getItem().hasContainerItem(input) && !StackUtil.isEmpty(input.getItem().getContainerItem(input))) {
         if (StackUtil.getSize(input) != recipe.getInput().getAmount()) {
            return null;
         }

         adjustedInput = StackUtil.copy(input);
      } else {
         adjustedInput = StackUtil.copyWithSize(input, StackUtil.getSize(input) - recipe.getInput().getAmount());
      }

      return recipe.getResult(adjustedInput);
   }

   private MachineRecipe<IRecipeInput, Collection<ItemStack>> getCollidingRecipe(IRecipeInput input) {
      for (ItemStack itemStackIn : input.getInputs()) {
         MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(itemStackIn, false);
         if (recipe != null) {
            return recipe;
         }
      }

      return null;
   }

   private MachineRecipe<IRecipeInput, Collection<ItemStack>> getRecipe(ItemStack stack, boolean checkAmount) {
      for (MachineRecipe<IRecipeInput, Collection<ItemStack>> container : this.recipes) {
         if (container.getInput().matches(stack)) {
            if (!checkAmount) {
               return container;
            }

            if (StackUtil.getSize(stack) >= container.getInput().getAmount()
               && (!stack.getItem().hasContainerItem(stack) || StackUtil.getSize(stack) == container.getInput().getAmount())) {
               return container;
            }
         }
      }

      return null;
   }

   @Override
   public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      return this.recipes;
   }

   @Override
   public boolean isIterable() {
      return true;
   }
}
