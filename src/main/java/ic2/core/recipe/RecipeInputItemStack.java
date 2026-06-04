package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import ic2.core.util.StackUtil;
import java.util.Arrays;
import java.util.List;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStack extends RecipeInputBase implements IRecipeInput {
  public final ItemStack input;
  
  public final int amount;
  
  RecipeInputItemStack(ItemStack input) {
    this(input, StackUtil.getSize(input));
  }
  
  RecipeInputItemStack(ItemStack input, int amount) {
    if (StackUtil.isEmpty(input))
      throw new IllegalArgumentException("invalid input stack"); 
    this.input = input.copy();
    this.amount = amount;
  }
  
  public boolean matches(ItemStack subject) {
    return (subject.getItem() == this.input.getItem() && (subject
      .func_77960_j() == this.input.func_77960_j() || this.input.func_77960_j() == 32767) && (this.input
      .func_77960_j() == 32767 || StackUtil.matchesNBT(subject.func_77978_p(), this.input.func_77978_p())));
  }
  
  public int getAmount() {
    return this.amount;
  }
  
  public List<ItemStack> getInputs() {
    return Arrays.asList(new ItemStack[] { StackUtil.setImmutableSize(this.input, getAmount()) });
  }
  
  public String toString() {
    return "RInputItemStack<" + StackUtil.setImmutableSize(this.input, this.amount) + ">";
  }
  
  public boolean equals(Object obj) {
    RecipeInputItemStack other;
    return (obj != null && getClass() == obj.getClass() && 
      StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStack)obj).input, this.input) && other.amount == this.amount);
  }
}
