// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.ingredients.IIngredients;
import net.minecraft.item.ItemStack;
import java.util.List;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IRecipeInput;
import net.minecraftforge.fluids.FluidStack;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class CannerEnrichmentWrapper extends BlankRecipeWrapper
{
    private final FluidStack input;
    private final FluidStack output;
    private final IRecipeInput additive;
    final IORecipeCategory<ICannerEnrichRecipeManager> category;
    
    CannerEnrichmentWrapper(final ICannerEnrichRecipeManager.Input input, final FluidStack output, final IORecipeCategory<ICannerEnrichRecipeManager> category) {
        this.input = input.fluid;
        this.additive = input.additive;
        this.output = output;
        this.category = category;
    }
    
    public FluidStack getInput() {
        return this.input;
    }
    
    public List<ItemStack> getAdditives() {
        return this.additive.getInputs();
    }
    
    public FluidStack getOutput() {
        return this.output;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInput((Class)FluidStack.class, (Object)this.getInput());
        ingredients.setInputs((Class)ItemStack.class, (List)this.getAdditives());
        ingredients.setOutput((Class)FluidStack.class, (Object)this.getOutput());
    }
}
