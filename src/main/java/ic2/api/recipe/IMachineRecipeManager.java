// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.nbt.NBTTagCompound;

public interface IMachineRecipeManager<RI, RO, I>
{
    boolean addRecipe(final RI p0, final RO p1, final NBTTagCompound p2, final boolean p3);
    
    MachineRecipeResult<RI, RO, I> apply(final I p0, final boolean p1);
    
    Iterable<? extends MachineRecipe<RI, RO>> getRecipes();
    
    boolean isIterable();
}
