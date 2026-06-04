// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import java.util.Comparator;
import it.unimi.dsi.fastutil.ints.IntComparators;
import net.minecraft.client.util.RecipeItemHelper;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import javax.annotation.Nullable;
import it.unimi.dsi.fastutil.ints.IntList;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.crafting.Ingredient;

public class IngredientRecipeInput extends Ingredient
{
    private IRecipeInput part;
    private ItemStack[] items;
    private IntList list;
    
    IngredientRecipeInput(final IRecipeInput part) {
        super(0);
        this.part = part;
    }
    
    public ItemStack[] getMatchingStacks() {
        if (this.items == null) {
            this.items = this.part.getInputs().toArray(new ItemStack[0]);
        }
        return this.items;
    }
    
    public boolean apply(@Nullable final ItemStack item) {
        return this.part.matches(item);
    }
    
    @SideOnly(Side.CLIENT)
    public IntList getValidItemStacksPacked() {
        if (this.list == null) {
            final ItemStack[] items = this.getMatchingStacks();
            this.list = (IntList)new IntArrayList(items.length);
            for (final ItemStack itemstack : items) {
                this.list.add(RecipeItemHelper.pack(itemstack));
            }
            this.list.sort((Comparator)IntComparators.NATURAL_COMPARATOR);
        }
        return this.list;
    }
}
