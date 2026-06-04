// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

public abstract class RecipeIngredient<T>
{
    public final T ingredient;
    
    public RecipeIngredient(final T ingredient) {
        this.ingredient = ingredient;
    }
    
    public abstract boolean isEmpty();
    
    public abstract boolean matches(final Object p0);
    
    public abstract boolean matchesStrict(final Object p0);
    
    public abstract String toStringSafe();
}
