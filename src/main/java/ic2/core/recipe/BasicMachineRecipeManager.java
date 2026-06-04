// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import ic2.core.init.MainConfig;
import java.util.ListIterator;
import ic2.api.recipe.RecipeOutput;
import java.util.List;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import java.util.ArrayList;
import java.util.Arrays;
import net.minecraft.nbt.NBTTagCompound;
import java.util.Iterator;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.IBasicMachineRecipeManager;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;

public class BasicMachineRecipeManager extends MachineRecipeHelper<IRecipeInput, Collection<ItemStack>> implements IBasicMachineRecipeManager
{
    @Override
    protected IRecipeInput getForInput(final IRecipeInput input) {
        return input;
    }
    
    @Override
    protected boolean consumeContainer(final ItemStack input, final ItemStack inContainer, final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe) {
        for (final ItemStack output : recipe.getOutput()) {
            if (StackUtil.checkItemEqualityStrict(inContainer, output)) {
                return true;
            }
            if (output.getItem().hasContainerItem(output) && StackUtil.checkItemEqualityStrict(input, output.getItem().getContainerItem(output))) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final NBTTagCompound metadata, final boolean replace, final ItemStack... outputs) {
        return this.addRecipe(input, Arrays.asList(outputs), metadata, replace);
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
        if (input == null) {
            throw new NullPointerException("null recipe input");
        }
        if (output == null) {
            throw new NullPointerException("null recipe output");
        }
        if (output.isEmpty()) {
            throw new IllegalArgumentException("no outputs");
        }
        final List<ItemStack> items = new ArrayList<ItemStack>(output.size());
        for (final ItemStack stack : output) {
            if (StackUtil.isEmpty(stack)) {
                this.displayError("The output ItemStack " + StackUtil.toStringSafe(stack) + " is invalid.");
                return false;
            }
            if (input.matches(stack) && (metadata == null || !metadata.hasKey("ignoreSameInputOutput"))) {
                this.displayError("The output ItemStack " + stack.toString() + " is the same as the recipe input " + input + ".");
                return false;
            }
            items.add(stack.copy());
        }
        for (final ItemStack is : input.getInputs()) {
            MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(is);
            if (recipe != null) {
                if (!replace) {
                    IC2.log.debug(LogCategory.Recipe, "Skipping %s => %s due to duplicate recipe for %s (%s => %s)", input, output, is, recipe.getInput(), recipe.getOutput());
                    return false;
                }
                do {
                    this.recipes.remove(input);
                    ((MachineRecipeHelper<IRecipeInput, RO>)this).removeCachedRecipes(input);
                    recipe = this.getRecipe(is);
                } while (recipe != null);
            }
        }
        final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe2 = new MachineRecipe<IRecipeInput, Collection<ItemStack>>(input, items, metadata);
        this.recipes.put((RI)input, (MachineRecipe<RI, RO>)recipe2);
        this.addToCache(recipe2);
        return true;
    }
    
    @Override
    public RecipeOutput getOutputFor(final ItemStack input, final boolean adjustInput) {
        final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input);
        if (recipe == null) {
            return null;
        }
        if (StackUtil.getSize(input) >= recipe.getInput().getAmount() && (!input.getItem().hasContainerItem(input) || StackUtil.getSize(input) == recipe.getInput().getAmount())) {
            if (adjustInput) {
                if (input.getItem().hasContainerItem(input)) {
                    throw new UnsupportedOperationException("can't adjust input item, use apply() instead");
                }
                input.shrink(recipe.getInput().getAmount());
            }
            return new RecipeOutput(recipe.getMetaData(), new ArrayList<ItemStack>(recipe.getOutput()));
        }
        return null;
    }
    
    public void removeRecipe(final ItemStack input, final Collection<ItemStack> output) {
        final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input);
        if (recipe != null && checkListEquality(recipe.getOutput(), output)) {
            this.recipes.remove(recipe.getInput());
            ((MachineRecipeHelper<IRecipeInput, RO>)this).removeCachedRecipes(recipe.getInput());
        }
    }
    
    private static boolean checkListEquality(final Collection<ItemStack> a, final Collection<ItemStack> b) {
        if (a.size() != b.size()) {
            return false;
        }
        final ListIterator<ItemStack> itB = new ArrayList<ItemStack>(b).listIterator();
    Label_0036:
        for (final ItemStack stack : a) {
            while (itB.hasNext()) {
                if (StackUtil.checkItemEqualityStrict(stack, itB.next())) {
                    itB.remove();
                    while (itB.hasPrevious()) {
                        itB.previous();
                    }
                    continue Label_0036;
                }
            }
            return false;
        }
        return true;
    }
    
    private void displayError(final String msg) {
        if (MainConfig.ignoreInvalidRecipes) {
            IC2.log.warn(LogCategory.Recipe, msg);
            return;
        }
        throw new RuntimeException(msg);
    }
}
