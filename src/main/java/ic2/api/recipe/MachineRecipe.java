// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.nbt.NBTTagCompound;

public class MachineRecipe<I, O>
{
    private final I input;
    private final O output;
    private final NBTTagCompound meta;
    
    public MachineRecipe(final I input, final O output) {
        this(input, output, null);
    }
    
    public MachineRecipe(final I input, final O output, final NBTTagCompound meta) {
        this.input = input;
        this.output = output;
        this.meta = meta;
    }
    
    public I getInput() {
        return this.input;
    }
    
    public O getOutput() {
        return this.output;
    }
    
    public NBTTagCompound getMetaData() {
        return this.meta;
    }
    
    public <AI> MachineRecipeResult<I, O, AI> getResult(final AI adjustedInput) {
        return new MachineRecipeResult<I, O, AI>(this, adjustedInput);
    }
}
