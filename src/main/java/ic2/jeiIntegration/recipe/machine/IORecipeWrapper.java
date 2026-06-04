// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.ingredients.IIngredients;
import javax.annotation.Nonnull;
import net.minecraft.client.Minecraft;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import ic2.api.recipe.MachineRecipe;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class IORecipeWrapper extends BlankRecipeWrapper
{
    private final IRecipeInput input;
    private final Collection<ItemStack> output;
    final IORecipeCategory<?> category;
    
    IORecipeWrapper(final MachineRecipe<IRecipeInput, Collection<ItemStack>> container, final IORecipeCategory<?> category) {
        this(container.getInput(), container.getOutput(), category);
    }
    
    protected IORecipeWrapper(final IRecipeInput input, final Collection<ItemStack> output, final IORecipeCategory<?> category) {
        this.input = input;
        this.output = output;
        this.category = category;
    }
    
    public List<List<ItemStack>> getInputs() {
        final List<ItemStack> inputs = this.input.getInputs();
        if (inputs.isEmpty()) {
            return Collections.emptyList();
        }
        return Collections.singletonList(inputs);
    }
    
    public List<ItemStack> getOutputs() {
        return new ArrayList<ItemStack>(this.output);
    }
    
    public void drawInfo(@Nonnull final Minecraft minecraft, final int recipeWidth, final int recipeHeight, final int mouseX, final int mouseY) {
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInputLists((Class)ItemStack.class, (List)this.getInputs());
        ingredients.setOutputs((Class)ItemStack.class, (List)this.getOutputs());
    }
}
