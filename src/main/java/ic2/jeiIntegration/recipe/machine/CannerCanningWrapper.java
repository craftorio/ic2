// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import java.util.Arrays;
import mezz.jei.api.ingredients.IIngredients;
import java.util.List;
import ic2.api.recipe.ICannerBottleRecipeManager;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class CannerCanningWrapper extends BlankRecipeWrapper
{
    private final IRecipeInput input;
    private final IRecipeInput can;
    private final ItemStack output;
    final IORecipeCategory<ICannerBottleRecipeManager> category;
    
    CannerCanningWrapper(final ICannerBottleRecipeManager.Input input, final ItemStack output, final IORecipeCategory<ICannerBottleRecipeManager> category) {
        this.input = input.fill;
        this.can = input.container;
        this.output = output;
        this.category = category;
    }
    
    public List<ItemStack> getInput() {
        return this.input.getInputs();
    }
    
    public List<ItemStack> getCan() {
        return this.can.getInputs();
    }
    
    public ItemStack getOutput() {
        return this.output;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputLists((Class)ItemStack.class, (List)Arrays.asList(this.getInput(), this.getCan()));
        ingredients.setOutput((Class)ItemStack.class, (Object)this.getOutput());
    }
}
