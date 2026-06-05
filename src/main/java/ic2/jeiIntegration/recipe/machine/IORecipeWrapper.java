package ic2.jeiIntegration.recipe.machine;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javax.annotation.Nonnull;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class IORecipeWrapper extends BlankRecipeWrapper {
   private final IRecipeInput input;
   private final Collection<ItemStack> output;
   final IORecipeCategory<?> category;

   IORecipeWrapper(MachineRecipe<IRecipeInput, Collection<ItemStack>> container, IORecipeCategory<?> category) {
      this(container.getInput(), container.getOutput(), category);
   }

   protected IORecipeWrapper(IRecipeInput input, Collection<ItemStack> output, IORecipeCategory<?> category) {
      this.input = input;
      this.output = output;
      this.category = category;
   }

   public List<List<ItemStack>> getInputs() {
      List<ItemStack> inputs = this.input.getInputs();
      return inputs.isEmpty() ? Collections.emptyList() : Collections.singletonList(inputs);
   }

   public List<ItemStack> getOutputs() {
      return new ArrayList<>(this.output);
   }

   public void drawInfo(@Nonnull Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
   }

   public void getIngredients(IIngredients ingredients) {
      ingredients.setInputLists(ItemStack.class, this.getInputs());
      ingredients.setOutputs(ItemStack.class, this.getOutputs());
   }
}
