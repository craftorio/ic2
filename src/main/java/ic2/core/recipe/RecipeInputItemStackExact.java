package ic2.core.recipe;

import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStackExact extends RecipeInputBase {
  public final ItemStack input;
  
  public final int amount;
  
  RecipeInputItemStackExact(ItemStack input) {
    this(input, StackUtil.getSize(input));
  }
  
  RecipeInputItemStackExact(ItemStack input, int amount) {
    if (StackUtil.isEmpty(input) || input.getMetadata() == 32767)
      throw new IllegalArgumentException("invalid input stack"); 
    this.input = StackUtil.copy(input);
    this.amount = amount;
  }
  
  public boolean matches(ItemStack subject) {
    return (subject.getItem() == this.input.getItem() && subject
      .getMetadata() == this.input.getMetadata() && 
      StackUtil.checkNbtEqualityStrict(subject, this.input));
  }
  
  public int getAmount() {
    return this.amount;
  }
  
  public List<ItemStack> getInputs() {
    return Collections.singletonList(StackUtil.setImmutableSize(this.input, getAmount()));
  }
  
  public String toString() {
    return "RInputItemStackExact<" + StackUtil.setImmutableSize(this.input, this.amount) + '>';
  }
  
  public boolean equals(Object obj) {
    RecipeInputItemStackExact other;
    return (obj != null && getClass() == obj.getClass() && 
      StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStackExact)obj).input, this.input) && other.amount == this.amount);
  }
}
