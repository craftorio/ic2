package ic2.api.recipe;

import net.minecraft.nbt.NBTTagCompound;

public interface IMachineRecipeManager<RI, RO, I> {
  boolean addRecipe(RI paramRI, RO paramRO, NBTTagCompound paramNBTTagCompound, boolean paramBoolean);
  
  MachineRecipeResult<RI, RO, I> apply(I paramI, boolean paramBoolean);
  
  Iterable<? extends MachineRecipe<RI, RO>> getRecipes();
  
  boolean isIterable();
}
