// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import java.util.Collection;

public interface IBasicMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
{
    boolean addRecipe(final IRecipeInput p0, final NBTTagCompound p1, final boolean p2, final ItemStack... p3);
    
    @Deprecated
    RecipeOutput getOutputFor(final ItemStack p0, final boolean p1);
}
