// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;
import net.minecraft.item.ItemStack;

public interface IRecipeInputFactory
{
    IRecipeInput forStack(final ItemStack p0);
    
    IRecipeInput forStack(final ItemStack p0, final int p1);
    
    IRecipeInput forExactStack(final ItemStack p0);
    
    IRecipeInput forExactStack(final ItemStack p0, final int p1);
    
    IRecipeInput forOreDict(final String p0);
    
    IRecipeInput forOreDict(final String p0, final int p1);
    
    IRecipeInput forOreDict(final String p0, final int p1, final int p2);
    
    IRecipeInput forFluidContainer(final Fluid p0);
    
    IRecipeInput forFluidContainer(final Fluid p0, final int p1);
    
    IRecipeInput forAny(final IRecipeInput... p0);
    
    IRecipeInput forAny(final Iterable<IRecipeInput> p0);
    
    IRecipeInput forIngredient(final Ingredient p0);
    
    Ingredient getIngredient(final IRecipeInput p0);
}
