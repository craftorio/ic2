package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class RecipeInputMultiple extends RecipeInputBase implements IRecipeInput {
   private final IRecipeInput[] inputs;

   RecipeInputMultiple(IRecipeInput... inputs) {
      this.inputs = inputs;
   }

   RecipeInputMultiple(List<IRecipeInput> inputs) {
      this.inputs = inputs.toArray(new IRecipeInput[0]);
   }

   @Override
   public boolean matches(ItemStack subject) {
      for (IRecipeInput input : this.inputs) {
         if (input.matches(subject)) {
            return true;
         }
      }

      return false;
   }

   @Override
   public int getAmount() {
      return 1;
   }

   @Override
   public List<ItemStack> getInputs() {
      List<ItemStack> list = new ArrayList<>();

      for (IRecipeInput input : this.inputs) {
         list.addAll(input.getInputs());
      }

      return Collections.unmodifiableList(list);
   }

   @Override
   public String toString() {
      if (this.inputs.length <= 0) {
         return "RecipeInputMultiple<Nothing>";
      }

      StringBuilder b = new StringBuilder("RecipeInputMultiple<");
      int i = 0;
      int end = this.inputs.length - 1;

      while (true) {
         b.append(this.inputs[i].toString());
         if (i == end) {
            return b.append('>').toString();
         }

         b.append(", ");
         i++;
      }
   }

   @Override
   public boolean equals(Object obj) {
      if (obj != null && this.getClass() == obj.getClass()) {
         IRecipeInput[] otherInputs = ((RecipeInputMultiple)obj).inputs;
         if (this.inputs.length == otherInputs.length) {
            for (int i = 0; i < this.inputs.length; i++) {
               if (!this.inputs[i].equals(otherInputs[i])) {
                  return false;
               }
            }

            return true;
         }
      }

      return false;
   }
}
