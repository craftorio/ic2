// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

public class MachineRecipeResult<RI, RO, I>
{
    private final MachineRecipe<RI, RO> recipe;
    private final I adjustedInput;
    
    public MachineRecipeResult(final MachineRecipe<RI, RO> recipe, final I adjustedInput) {
        this.recipe = recipe;
        this.adjustedInput = adjustedInput;
    }
    
    public MachineRecipe<RI, RO> getRecipe() {
        return this.recipe;
    }
    
    public RO getOutput() {
        return this.recipe.getOutput();
    }
    
    public I getAdjustedInput() {
        return this.adjustedInput;
    }
}
