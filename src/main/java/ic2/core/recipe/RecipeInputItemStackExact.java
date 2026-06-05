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
      if (!StackUtil.isEmpty(input) && input.getMetadata() != 32767) {
         this.input = StackUtil.copy(input);
         this.amount = amount;
      } else {
         throw new IllegalArgumentException("invalid input stack");
      }
   }

   @Override
   public boolean matches(ItemStack subject) {
      return subject.getItem() == this.input.getItem()
         && subject.getMetadata() == this.input.getMetadata()
         && StackUtil.checkNbtEqualityStrict(subject, this.input);
   }

   @Override
   public int getAmount() {
      return this.amount;
   }

   @Override
   public List<ItemStack> getInputs() {
      return Collections.singletonList(StackUtil.setImmutableSize(this.input, this.getAmount()));
   }

   @Override
   public String toString() {
      return "RInputItemStackExact<" + StackUtil.setImmutableSize(this.input, this.amount) + '>';
   }

   @Override
   public boolean equals(Object obj) {
      RecipeInputItemStackExact other;
      return obj != null
         && this.getClass() == obj.getClass()
         && StackUtil.checkItemEqualityStrict((other = (RecipeInputItemStackExact)obj).input, this.input)
         && other.amount == this.amount;
   }
}
