package ic2.core.recipe.dynamic;

public abstract class RecipeOutputIngredient<T> extends RecipeIngredient<T> {
  protected RecipeOutputIngredient(T ingredient) {
    super(ingredient);
  }
  
  public abstract RecipeOutputIngredient<T> copy();
}
