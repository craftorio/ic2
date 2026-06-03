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
    this.inputs = inputs.<IRecipeInput>toArray(new IRecipeInput[0]);
  }
  
  public boolean matches(ItemStack subject) {
    for (IRecipeInput input : this.inputs) {
      if (input.matches(subject))
        return true; 
    } 
    return false;
  }
  
  public int getAmount() {
    return 1;
  }
  
  public List<ItemStack> getInputs() {
    List<ItemStack> list = new ArrayList<>();
    for (IRecipeInput input : this.inputs)
      list.addAll(input.getInputs()); 
    return Collections.unmodifiableList(list);
  }
  
  public String toString() {
    if (this.inputs.length <= 0)
      return "RecipeInputMultiple<Nothing>"; 
    StringBuilder b = new StringBuilder("RecipeInputMultiple<");
    for (int i = 0, end = this.inputs.length - 1;; i++) {
      b.append(this.inputs[i].toString());
      if (i == end)
        return b.append('>').toString(); 
      b.append(", ");
    } 
  }
  
  public boolean equals(Object obj) {
    if (obj != null && getClass() == obj.getClass()) {
      IRecipeInput[] otherInputs = ((RecipeInputMultiple)obj).inputs;
      if (this.inputs.length == otherInputs.length) {
        for (int i = 0; i < this.inputs.length; i++) {
          if (!this.inputs[i].equals(otherInputs[i]))
            return false; 
        } 
        return true;
      } 
    } 
    return false;
  }
}
