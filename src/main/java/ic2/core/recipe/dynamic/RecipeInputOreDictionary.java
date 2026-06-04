// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe.dynamic;

import net.minecraftforge.oredict.OreDictionary;
import java.util.Iterator;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import java.util.List;

public class RecipeInputOreDictionary extends RecipeInputIngredient<String>
{
    public final int amount;
    public final Integer meta;
    private List<ItemStack> equivalents;
    
    public static RecipeInputOreDictionary of(final String ingredient) {
        return of(ingredient, 1);
    }
    
    public static RecipeInputOreDictionary of(final String ingredient, final int amount) {
        return of(ingredient, amount, null);
    }
    
    public static RecipeInputOreDictionary of(final String ingredient, final int amount, final Integer meta) {
        return new RecipeInputOreDictionary(ingredient, amount, meta);
    }
    
    protected RecipeInputOreDictionary(final String ingredient) {
        this(ingredient, 1);
    }
    
    protected RecipeInputOreDictionary(final String ingredient, final int amount) {
        this(ingredient, amount, null);
    }
    
    protected RecipeInputOreDictionary(final String ingredient, final int amount, final Integer meta) {
        super(ingredient);
        this.amount = amount;
        this.meta = meta;
    }
    
    @Override
    public Object getUnspecific() {
        return null;
    }
    
    @Override
    public RecipeInputIngredient<String> copy() {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public boolean isEmpty() {
        return this.amount <= 0;
    }
    
    @Override
    public int getCount() {
        return this.amount;
    }
    
    @Override
    public void shrink(final int amount) {
        throw new UnsupportedOperationException("Not supported");
    }
    
    @Override
    public boolean matches(final Object other) {
        if (!(other instanceof ItemStack)) {
            return false;
        }
        final List<ItemStack> inputs = this.getEquivalents();
        final boolean useOreStackMeta = this.meta == null;
        final Item subjectItem = ((ItemStack)other).getItem();
        final int subjectMeta = ((ItemStack)other).getItemDamage();
        for (final ItemStack oreStack : inputs) {
            final Item oreItem = oreStack.getItem();
            if (oreItem == null) {
                continue;
            }
            final int metaRequired = useOreStackMeta ? oreStack.getItemDamage() : this.meta;
            if (subjectItem == oreItem && (subjectMeta == metaRequired || metaRequired == 32767)) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean matchesStrict(final Object other) {
        return other instanceof String && ((String)this.ingredient).equals(other);
    }
    
    @Override
    public String toStringSafe() {
        return (String)this.ingredient;
    }
    
    private List<ItemStack> getEquivalents() {
        if (this.equivalents != null) {
            return this.equivalents;
        }
        final List<ItemStack> ret = (List<ItemStack>)OreDictionary.getOres((String)this.ingredient);
        if (ret != OreDictionary.EMPTY_LIST) {
            this.equivalents = ret;
        }
        return ret;
    }
}
