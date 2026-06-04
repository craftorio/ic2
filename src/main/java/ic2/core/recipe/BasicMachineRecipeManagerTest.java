// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Iterator;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import java.util.Arrays;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import java.util.List;
import ic2.api.recipe.IBasicMachineRecipeManager;

public class BasicMachineRecipeManagerTest implements IBasicMachineRecipeManager
{
    private final List<MachineRecipe<IRecipeInput, Collection<ItemStack>>> recipes;
    
    public BasicMachineRecipeManagerTest() {
        this.recipes = new ArrayList<MachineRecipe<IRecipeInput, Collection<ItemStack>>>();
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
        if (replace) {
            this.recipes.add(0, new MachineRecipe<IRecipeInput, Collection<ItemStack>>(input, output, metadata));
        }
        else {
            if (this.getCollidingRecipe(input) != null) {
                return false;
            }
            this.recipes.add(new MachineRecipe<IRecipeInput, Collection<ItemStack>>(input, output, metadata));
        }
        return true;
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput input, final NBTTagCompound metadata, final boolean replace, final ItemStack... outputs) {
        return this.addRecipe(input, (Collection<ItemStack>)Arrays.asList(outputs), metadata, replace);
    }
    
    @Override
    public RecipeOutput getOutputFor(final ItemStack input, final boolean adjustInput) {
        final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input, true);
        if (recipe == null) {
            return null;
        }
        if (adjustInput) {
            if (input.getItem().hasContainerItem(input)) {
                throw new UnsupportedOperationException("can't adjust input item, use apply() instead");
            }
            input.shrink(recipe.getInput().getAmount());
        }
        return new RecipeOutput(recipe.getMetaData(), new ArrayList<ItemStack>(recipe.getOutput()));
    }
    
    @Override
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        if (StackUtil.isEmpty(input)) {
            return null;
        }
        final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(input, true);
        if (recipe == null) {
            return null;
        }
        ItemStack adjustedInput;
        if (input.getItem().hasContainerItem(input) && !StackUtil.isEmpty(adjustedInput = input.getItem().getContainerItem(input))) {
            if (StackUtil.getSize(input) != recipe.getInput().getAmount()) {
                return null;
            }
            adjustedInput = StackUtil.copy(input);
        }
        else {
            adjustedInput = StackUtil.copyWithSize(input, StackUtil.getSize(input) - recipe.getInput().getAmount());
        }
        return recipe.getResult(adjustedInput);
    }
    
    private MachineRecipe<IRecipeInput, Collection<ItemStack>> getCollidingRecipe(final IRecipeInput input) {
        for (final ItemStack itemStackIn : input.getInputs()) {
            final MachineRecipe<IRecipeInput, Collection<ItemStack>> recipe = this.getRecipe(itemStackIn, false);
            if (recipe != null) {
                return recipe;
            }
        }
        return null;
    }
    
    private MachineRecipe<IRecipeInput, Collection<ItemStack>> getRecipe(final ItemStack stack, final boolean checkAmount) {
        for (final MachineRecipe<IRecipeInput, Collection<ItemStack>> container : this.recipes) {
            if (container.getInput().matches(stack)) {
                if (!checkAmount) {
                    return container;
                }
                if (StackUtil.getSize(stack) >= container.getInput().getAmount() && (!stack.getItem().hasContainerItem(stack) || StackUtil.getSize(stack) == container.getInput().getAmount())) {
                    return container;
                }
                continue;
            }
        }
        return null;
    }
    
    @Override
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
        return this.recipes;
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
}
