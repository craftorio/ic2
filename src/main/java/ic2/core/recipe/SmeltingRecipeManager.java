// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import java.util.Collections;
import java.util.Iterator;
import ic2.api.recipe.Recipes;
import com.google.common.collect.Iterables;
import java.util.Collection;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.util.StackUtil;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IMachineRecipeManager;

public class SmeltingRecipeManager implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack>
{
    @Override
    public boolean addRecipe(final ItemStack input, final ItemStack output, final NBTTagCompound metadata, final boolean replace) {
        final FurnaceRecipes recipes = FurnaceRecipes.instance();
        if (!StackUtil.isEmpty(recipes.getSmeltingResult(input)) && !replace) {
            return false;
        }
        final float experience = (metadata != null && metadata.hasKey("experience")) ? metadata.getFloat("experience") : 0.0f;
        if (experience < 0.0f) {
            throw new IllegalArgumentException("Negative xp for " + StackUtil.toStringSafe(input) + " -> " + StackUtil.toStringSafe(output));
        }
        recipes.addSmeltingRecipe(input, output, experience);
        return true;
    }
    
    @Override
    public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        final FurnaceRecipes recipes = FurnaceRecipes.instance();
        final ItemStack output = recipes.getSmeltingResult(input);
        if (StackUtil.isEmpty(output)) {
            return null;
        }
        final NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("experience", recipes.getSmeltingExperience(output) * StackUtil.getSize(output));
        return new MachineRecipe<ItemStack, ItemStack>(input, output, nbt).getResult(StackUtil.copyShrunk(input, 1));
    }
    
    @Override
    public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isIterable() {
        return false;
    }
    
    public enum SmeltingBridge implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
    {
        INSTANCE;
        
        @Override
        public boolean addRecipe(final IRecipeInput input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
            final ItemStack realOutput = (ItemStack)Iterables.getOnlyElement((Iterable)output);
            boolean ret = false;
            for (final ItemStack stack : input.getInputs()) {
                ret |= Recipes.furnace.addRecipe(stack, realOutput, metadata, replace);
            }
            return ret;
        }
        
        @Override
        public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
            final MachineRecipeResult<ItemStack, ItemStack, ItemStack> normal = Recipes.furnace.apply(input, acceptTest);
            if (normal == null) {
                return null;
            }
            final MachineRecipe<ItemStack, ItemStack> result = normal.getRecipe();
            final IRecipeInput resultIn = Recipes.inputFactory.forStack(result.getInput());
            final Collection<ItemStack> resultOut = Collections.singletonList(result.getOutput());
            final NBTTagCompound resultNBT = result.getMetaData();
            return new MachineRecipe<IRecipeInput, Collection<ItemStack>>(resultIn, resultOut, resultNBT).getResult(normal.getAdjustedInput());
        }
        
        @Override
        public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean isIterable() {
            return false;
        }
    }
}
