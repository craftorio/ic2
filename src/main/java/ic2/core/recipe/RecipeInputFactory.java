// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraft.item.crafting.Ingredient;
import com.google.common.collect.Iterables;
import java.util.Collection;
import net.minecraftforge.fluids.Fluid;
import ic2.api.recipe.IRecipeInput;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInputFactory;

public class RecipeInputFactory implements IRecipeInputFactory
{
    @Override
    public IRecipeInput forStack(final ItemStack stack) {
        return new RecipeInputItemStack(stack);
    }
    
    @Override
    public IRecipeInput forStack(final ItemStack stack, final int amount) {
        return new RecipeInputItemStack(stack, amount);
    }
    
    @Override
    public IRecipeInput forExactStack(final ItemStack stack) {
        if (stack.getMetadata() == 32767) {
            return this.forStack(stack);
        }
        return new RecipeInputItemStackExact(stack);
    }
    
    @Override
    public IRecipeInput forExactStack(final ItemStack stack, final int amount) {
        if (stack.getMetadata() == 32767) {
            return this.forStack(stack, amount);
        }
        return new RecipeInputItemStackExact(stack, amount);
    }
    
    @Override
    public IRecipeInput forOreDict(final String name) {
        return new RecipeInputOreDict(name);
    }
    
    @Override
    public IRecipeInput forOreDict(final String name, final int amount) {
        return new RecipeInputOreDict(name, amount);
    }
    
    @Override
    public IRecipeInput forOreDict(final String name, final int amount, final int metaOverride) {
        return new RecipeInputOreDict(name, amount, metaOverride);
    }
    
    @Override
    public IRecipeInput forFluidContainer(final Fluid fluid) {
        return new RecipeInputFluidContainer(fluid);
    }
    
    @Override
    public IRecipeInput forFluidContainer(final Fluid fluid, final int amount) {
        return new RecipeInputFluidContainer(fluid, amount);
    }
    
    @Override
    public IRecipeInput forAny(final IRecipeInput... options) {
        return new RecipeInputMultiple(options);
    }
    
    @Override
    public IRecipeInput forAny(final Iterable<IRecipeInput> options) {
        if (options instanceof Collection) {
            return new RecipeInputMultiple((IRecipeInput[])((Collection)options).toArray(new IRecipeInput[0]));
        }
        return new RecipeInputMultiple((IRecipeInput[])Iterables.toArray((Iterable)options, (Class)IRecipeInput.class));
    }
    
    @Override
    public Ingredient getIngredient(final IRecipeInput input) {
        return new IngredientRecipeInput(input);
    }
    
    @Override
    public IRecipeInput forIngredient(final Ingredient ingredient) {
        return new RecipeInputIngredient(ingredient);
    }
}
