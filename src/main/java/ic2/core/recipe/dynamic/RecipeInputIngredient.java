package ic2.core.recipe.dynamic;

public abstract class RecipeInputIngredient<T> extends RecipeIngredient<T> {
   public final boolean consumable;

   protected RecipeInputIngredient(T ingredient) {
      this(ingredient, true);
   }

   protected RecipeInputIngredient(T ingredient, boolean consumable) {
      super(ingredient);
      this.consumable = consumable;
   }

   public abstract Object getUnspecific();

   public abstract RecipeInputIngredient<T> copy();

   public abstract int getCount();

   public abstract void shrink(int var1);

   public boolean isConsumable() {
      return this.consumable;
   }
}
