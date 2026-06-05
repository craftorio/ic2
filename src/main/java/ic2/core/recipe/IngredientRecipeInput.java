package ic2.core.recipe;

import ic2.api.recipe.IRecipeInput;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntComparators;
import it.unimi.dsi.fastutil.ints.IntList;
import javax.annotation.Nullable;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class IngredientRecipeInput extends Ingredient {
   private IRecipeInput part;
   private ItemStack[] items;
   private IntList list;

   IngredientRecipeInput(IRecipeInput part) {
      super(0);
      this.part = part;
   }

   public ItemStack[] getMatchingStacks() {
      if (this.items == null) {
         this.items = this.part.getInputs().toArray(new ItemStack[0]);
      }

      return this.items;
   }

   public boolean apply(@Nullable ItemStack item) {
      return this.part.matches(item);
   }

   @SideOnly(Side.CLIENT)
   public IntList getValidItemStacksPacked() {
      if (this.list == null) {
         ItemStack[] items = this.getMatchingStacks();
         this.list = new IntArrayList(items.length);

         for (ItemStack itemstack : items) {
            this.list.add(RecipeItemHelper.pack(itemstack));
         }

         this.list.sort(IntComparators.NATURAL_COMPARATOR);
      }

      return this.list;
   }
}
