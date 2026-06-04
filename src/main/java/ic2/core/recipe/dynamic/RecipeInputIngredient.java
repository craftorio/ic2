// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

public abstract class RecipeInputIngredient<T> extends RecipeIngredient<T>
{
    public final boolean consumable;
    
    protected RecipeInputIngredient(final T ingredient) {
        this(ingredient, true);
    }
    
    protected RecipeInputIngredient(final T ingredient, final boolean consumable) {
        super(ingredient);
        this.consumable = consumable;
    }
    
    public abstract Object getUnspecific();
    
    public abstract RecipeInputIngredient<T> copy();
    
    public abstract int getCount();
    
    public abstract void shrink(final int p0);
    
    public boolean isConsumable() {
        return this.consumable;
    }
}
