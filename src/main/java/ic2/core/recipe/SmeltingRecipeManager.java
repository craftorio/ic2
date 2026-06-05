package ic2.core.recipe;

import com.google.common.collect.Iterables;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class SmeltingRecipeManager implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack> {
   public boolean addRecipe(ItemStack input, ItemStack output, NBTTagCompound metadata, boolean replace) {
      FurnaceRecipes recipes = FurnaceRecipes.instance();
      if (!StackUtil.isEmpty(recipes.getSmeltingResult(input)) && !replace) {
         return false;
      }

      float experience = metadata != null && metadata.hasKey("experience") ? metadata.getFloat("experience") : 0.0F;
      if (experience < 0.0F) {
         throw new IllegalArgumentException("Negative xp for " + StackUtil.toStringSafe(input) + " -> " + StackUtil.toStringSafe(output));
      }

      recipes.addSmeltingRecipe(input, output, experience);
      return true;
   }

   public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(ItemStack input, boolean acceptTest) {
      FurnaceRecipes recipes = FurnaceRecipes.instance();
      ItemStack output = recipes.getSmeltingResult(input);
      if (StackUtil.isEmpty(output)) {
         return null;
      }

      NBTTagCompound nbt = new NBTTagCompound();
      nbt.setFloat("experience", recipes.getSmeltingExperience(output) * StackUtil.getSize(output));
      return new MachineRecipe<>(input, output, nbt).getResult(StackUtil.copyShrunk(input, 1));
   }

   @Override
   public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes() {
      throw new UnsupportedOperationException();
   }

   @Override
   public boolean isIterable() {
      return false;
   }

   public enum SmeltingBridge implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
      INSTANCE;

      public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
         ItemStack realOutput = (ItemStack)Iterables.getOnlyElement(output);
         boolean ret = false;

         for (ItemStack stack : input.getInputs()) {
            ret |= Recipes.furnace.addRecipe(stack, realOutput, metadata, replace);
         }

         return ret;
      }

      public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
         MachineRecipeResult<ItemStack, ItemStack, ItemStack> normal = Recipes.furnace.apply(input, acceptTest);
         if (normal == null) {
            return null;
         }

         MachineRecipe<ItemStack, ItemStack> result = normal.getRecipe();
         IRecipeInput resultIn = Recipes.inputFactory.forStack(result.getInput());
         Collection<ItemStack> resultOut = Collections.singletonList(result.getOutput());
         NBTTagCompound resultNBT = result.getMetaData();
         return new MachineRecipe<>(resultIn, resultOut, resultNBT).getResult(normal.getAdjustedInput());
      }

      @Override
      public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean isIterable() {
         return false;
      }
   }
}
