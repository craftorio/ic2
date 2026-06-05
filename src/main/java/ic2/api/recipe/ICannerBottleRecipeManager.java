package ic2.api.recipe;

import net.minecraft.item.ItemStack;

public interface ICannerBottleRecipeManager extends IMachineRecipeManager<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> {
   boolean addRecipe(IRecipeInput var1, IRecipeInput var2, ItemStack var3, boolean var4);

   @Deprecated
   void addRecipe(IRecipeInput var1, IRecipeInput var2, ItemStack var3);

   @Deprecated
   RecipeOutput getOutputFor(ItemStack var1, ItemStack var2, boolean var3, boolean var4);

   class Input {
      public final IRecipeInput container;
      public final IRecipeInput fill;

      public Input(IRecipeInput container, IRecipeInput fill) {
         this.container = container;
         this.fill = fill;
      }

      public boolean matches(ItemStack container, ItemStack fill) {
         return this.container.matches(container) && this.fill.matches(fill);
      }
   }

   class RawInput {
      public final ItemStack container;
      public final ItemStack fill;

      public RawInput(ItemStack container, ItemStack fill) {
         this.container = container;
         this.fill = fill;
      }
   }
}
