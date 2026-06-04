// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeOutputItemStack extends RecipeOutputIngredient<ItemStack>
{
    public static RecipeOutputItemStack of(final ItemStack ingredient) {
        return new RecipeOutputItemStack(ingredient);
    }
    
    protected RecipeOutputItemStack(final ItemStack ingredient) {
        super(ingredient);
    }
    
    @Override
    public RecipeOutputIngredient<ItemStack> copy() {
        return of(((ItemStack)this.ingredient).copy());
    }
    
    @Override
    public boolean isEmpty() {
        return StackUtil.isEmpty((ItemStack)this.ingredient);
    }
    
    @Override
    public boolean matches(final Object other) {
        return other instanceof ItemStack && StackUtil.checkItemEqualityStrict((ItemStack)this.ingredient, (ItemStack)other);
    }
    
    @Override
    public boolean matchesStrict(final Object other) {
        return this.matches(other);
    }
    
    @Override
    public String toStringSafe() {
        return StackUtil.toStringSafe((ItemStack)this.ingredient);
    }
}
