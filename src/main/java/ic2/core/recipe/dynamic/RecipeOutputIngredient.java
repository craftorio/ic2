// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

public abstract class RecipeOutputIngredient<T> extends RecipeIngredient<T>
{
    protected RecipeOutputIngredient(final T ingredient) {
        super(ingredient);
    }
    
    public abstract RecipeOutputIngredient<T> copy();
}
