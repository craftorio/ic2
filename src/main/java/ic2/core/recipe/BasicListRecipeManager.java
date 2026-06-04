// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import ic2.api.recipe.MachineRecipe;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.Iterator;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.recipe.IListRecipeManager;
import ic2.api.recipe.IRecipeInput;

public class BasicListRecipeManager extends MachineRecipeHelper<IRecipeInput, Object> implements IListRecipeManager
{
    private static final Object dummyOutput;
    
    @Override
    public void add(final IRecipeInput input) {
        if (input == null) {
            throw new NullPointerException("Input must not be null.");
        }
        this.addRecipe(input, BasicListRecipeManager.dummyOutput, null, false);
    }
    
    @Override
    public boolean contains(final ItemStack stack) {
        return !StackUtil.isEmpty(stack) && this.getRecipe(stack) != null;
    }
    
    @Override
    public boolean isEmpty() {
        return this.recipes.isEmpty();
    }
    
    @Override
    public List<IRecipeInput> getInputs() {
        return new ArrayList<IRecipeInput>((Collection<? extends IRecipeInput>)this.recipes.keySet());
    }
    
    @Override
    public Iterator<IRecipeInput> iterator() {
        return (Iterator<IRecipeInput>)this.recipes.keySet().iterator();
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final Object output, final NBTTagCompound metadata, final boolean replace) {
        for (final ItemStack is : input.getInputs()) {
            MachineRecipe<IRecipeInput, Object> recipe = this.getRecipe(is);
            if (recipe != null) {
                if (!replace) {
                    IC2.log.debug(LogCategory.Recipe, "Skipping %s due to duplicate recipe for %s (%s)", input, is, recipe.getInput());
                    return false;
                }
                do {
                    this.recipes.remove(input);
                    ((MachineRecipeHelper<IRecipeInput, RO>)this).removeCachedRecipes(input);
                    recipe = this.getRecipe(is);
                } while (recipe != null);
            }
        }
        final MachineRecipe<IRecipeInput, Object> recipe2 = new MachineRecipe<IRecipeInput, Object>(input, output, metadata);
        this.recipes.put((RI)input, (MachineRecipe<RI, RO>)recipe2);
        this.addToCache(recipe2);
        return false;
    }
    
    @Override
    protected IRecipeInput getForInput(final IRecipeInput input) {
        return input;
    }
    
    @Override
    protected boolean consumeContainer(final ItemStack input, final ItemStack container, final MachineRecipe<IRecipeInput, Object> recipe) {
        return true;
    }
    
    static {
        dummyOutput = new Object();
    }
}
