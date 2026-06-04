// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.recipe.machine;

import mezz.jei.api.ingredients.IIngredients;
import java.util.Collections;
import java.util.ArrayList;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;
import mezz.jei.api.recipe.BlankRecipeWrapper;

public class ElectrolyzerWrapper extends BlankRecipeWrapper
{
    private final FluidStack input;
    private final List<FluidStack> outputs;
    final IORecipeCategory<IElectrolyzerRecipeManager> category;
    
    ElectrolyzerWrapper(final FluidStack input, final IElectrolyzerRecipeManager.ElectrolyzerOutput[] outputs, final IORecipeCategory<IElectrolyzerRecipeManager> category) {
        this.input = input;
        final List<FluidStack> temp = new ArrayList<FluidStack>(outputs.length);
        for (final IElectrolyzerRecipeManager.ElectrolyzerOutput output : outputs) {
            temp.add(output.getOutput());
        }
        this.outputs = Collections.unmodifiableList((List<? extends FluidStack>)temp);
        this.category = category;
    }
    
    public FluidStack getFluidInput() {
        return this.input;
    }
    
    public List<FluidStack> getFluidOutputs() {
        return this.outputs;
    }
    
    public void getIngredients(final IIngredients ingredients) {
        ingredients.setInput((Class)FluidStack.class, (Object)this.getFluidInput());
        ingredients.setOutputs((Class)FluidStack.class, (List)this.getFluidOutputs());
    }
}
