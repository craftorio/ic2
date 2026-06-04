// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStack extends RecipeInputIngredient<ItemStack>
{
    public static RecipeInputItemStack of(final ItemStack ingredient) {
        return new RecipeInputItemStack(ingredient);
    }
    
    public static RecipeInputItemStack of(final ItemStack ingredient, final boolean consumable) {
        return new RecipeInputItemStack(ingredient, consumable);
    }
    
    protected RecipeInputItemStack(final ItemStack ingredient) {
        super(ingredient);
    }
    
    protected RecipeInputItemStack(final ItemStack ingredient, final boolean consumable) {
        super(ingredient, consumable);
    }
    
    @Override
    public Object getUnspecific() {
        return ((ItemStack)this.ingredient).getItem();
    }
    
    @Override
    public RecipeInputIngredient<ItemStack> copy() {
        return of(((ItemStack)this.ingredient).copy());
    }
    
    @Override
    public boolean isEmpty() {
        return StackUtil.isEmpty((ItemStack)this.ingredient);
    }
    
    @Override
    public int getCount() {
        return StackUtil.getSize((ItemStack)this.ingredient);
    }
    
    @Override
    public void shrink(final int amount) {
        ((ItemStack)this.ingredient).shrink(amount);
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
