// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import java.util.Iterator;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;
import ic2.api.recipe.MachineRecipe;
import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IMachineRecipeManager;

public class MatterAmplifierRecipeManager implements IMachineRecipeManager<IRecipeInput, Integer, ItemStack>
{
    private final List<MachineRecipe<IRecipeInput, Integer>> recipes;
    
    public MatterAmplifierRecipeManager() {
        this.recipes = new ArrayList<MachineRecipe<IRecipeInput, Integer>>();
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final Integer output, final NBTTagCompound metadata, final boolean replace) {
        if (output <= 0) {
            throw new IllegalArgumentException("non-positive amplification");
        }
        for (final ItemStack stack : input.getInputs()) {
            final MachineRecipe<IRecipeInput, Integer> recipe = this.getRecipe(stack, true);
            if (recipe != null) {
                if (!replace) {
                    return false;
                }
                this.recipes.remove(recipe);
            }
        }
        this.recipes.add(new MachineRecipe<IRecipeInput, Integer>(input, output));
        return true;
    }
    
    @Override
    public MachineRecipeResult<IRecipeInput, Integer, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        final MachineRecipe<IRecipeInput, Integer> recipe = this.getRecipe(input, acceptTest);
        if (recipe == null) {
            return null;
        }
        return recipe.getResult(StackUtil.copyShrunk(input, recipe.getInput().getAmount()));
    }
    
    private MachineRecipe<IRecipeInput, Integer> getRecipe(final ItemStack stack, final boolean acceptTest) {
        for (final MachineRecipe<IRecipeInput, Integer> recipe : this.recipes) {
            if (recipe.getInput().matches(stack) && (acceptTest || recipe.getInput().getAmount() <= StackUtil.getSize(stack))) {
                return recipe;
            }
        }
        return null;
    }
    
    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Integer>> getRecipes() {
        return this.recipes;
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
}
