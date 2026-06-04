// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine;

import ic2.api.recipe.MachineRecipeResult;
import ic2.core.util.StackUtil;
import ic2.api.recipe.RecipeOutput;
import java.util.Iterator;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.recipe.IRecipeInput;
import java.util.ArrayList;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.MachineRecipe;
import java.util.List;
import ic2.api.recipe.ICannerBottleRecipeManager;

public class CannerBottleRecipeManager implements ICannerBottleRecipeManager
{
    private final List<MachineRecipe<Input, ItemStack>> recipes;
    
    public CannerBottleRecipeManager() {
        this.recipes = new ArrayList<MachineRecipe<Input, ItemStack>>();
    }
    
    @Override
    public boolean addRecipe(final IRecipeInput container, final IRecipeInput fill, final ItemStack output, final boolean replace) {
        return this.addRecipe(new Input(container, fill), output, (NBTTagCompound)null, replace);
    }
    
    @Deprecated
    @Override
    public void addRecipe(final IRecipeInput container, final IRecipeInput fill, final ItemStack output) {
        if (!this.addRecipe(container, fill, output, false)) {
            throw new IllegalStateException("ambiguous canner bottle recipe: " + container + " + " + fill + " -> " + output);
        }
    }
    
    @Override
    public boolean addRecipe(final Input input, final ItemStack output, final NBTTagCompound metadata, final boolean replace) {
        final Iterator<MachineRecipe<Input, ItemStack>> it = this.recipes.iterator();
    Label_0011:
        while (true) {
            while (it.hasNext()) {
                final MachineRecipe<Input, ItemStack> recipe = it.next();
                for (final ItemStack containerStack : input.container.getInputs()) {
                    for (final ItemStack fillStack : input.fill.getInputs()) {
                        if (recipe.getInput().matches(containerStack, fillStack)) {
                            if (replace) {
                                it.remove();
                                continue Label_0011;
                            }
                            IC2.log.warn(LogCategory.Recipe, "ambiguous recipe: [" + input.container.getInputs() + "+" + input.fill.getInputs() + " -> " + output + "], conflicts with [" + recipe.getInput().container.getInputs() + "+" + recipe.getInput().fill.getInputs() + " -> " + recipe.getOutput() + "]");
                            return false;
                        }
                    }
                }
            }
            break;
        }
        this.recipes.add(new MachineRecipe<Input, ItemStack>(input, output));
        return true;
    }
    
    @Override
    public RecipeOutput getOutputFor(final ItemStack container, final ItemStack fill, final boolean adjustInput, final boolean acceptTest) {
        if (acceptTest) {
            if (StackUtil.isEmpty(container) && StackUtil.isEmpty(fill)) {
                return null;
            }
        }
        else if (StackUtil.isEmpty(container) || StackUtil.isEmpty(fill)) {
            return null;
        }
        for (final MachineRecipe<Input, ItemStack> recipe : this.recipes) {
            final Input recipeInput = recipe.getInput();
            if (acceptTest && StackUtil.isEmpty(container)) {
                if (recipeInput.fill.matches(fill)) {
                    return new RecipeOutput(null, new ItemStack[] { recipe.getOutput() });
                }
                continue;
            }
            else if (acceptTest && StackUtil.isEmpty(fill)) {
                if (recipeInput.container.matches(container)) {
                    return new RecipeOutput(null, new ItemStack[] { recipe.getOutput() });
                }
                continue;
            }
            else {
                if (!recipeInput.matches(container, fill)) {
                    continue;
                }
                if (acceptTest || (!StackUtil.isEmpty(container) && StackUtil.getSize(container) >= recipeInput.container.getAmount() && StackUtil.getSize(fill) >= recipeInput.fill.getAmount())) {
                    if (adjustInput) {
                        if (!StackUtil.isEmpty(container)) {
                            container.shrink(recipeInput.container.getAmount());
                        }
                        fill.shrink(recipeInput.fill.getAmount());
                    }
                    new RecipeOutput(null, new ItemStack[] { recipe.getOutput() });
                    break;
                }
                break;
            }
        }
        return null;
    }
    
    @Override
    public MachineRecipeResult<Input, ItemStack, RawInput> apply(final RawInput input, final boolean acceptTest) {
        final boolean emptyContainer = StackUtil.isEmpty(input.container);
        final boolean emptyFill = StackUtil.isEmpty(input.fill);
        if (!acceptTest && (emptyContainer || emptyFill)) {
            return null;
        }
        if (acceptTest && emptyContainer && emptyFill) {
            return null;
        }
        for (final MachineRecipe<Input, ItemStack> recipe : this.recipes) {
            if ((emptyContainer || (recipe.getInput().container.matches(input.container) && recipe.getInput().container.getAmount() <= StackUtil.getSize(input.container))) && (emptyFill || (recipe.getInput().fill.matches(input.fill) && recipe.getInput().fill.getAmount() <= StackUtil.getSize(input.fill)))) {
                return recipe.getResult(new RawInput(emptyContainer ? StackUtil.emptyStack : StackUtil.copyShrunk(input.container, recipe.getInput().container.getAmount()), emptyFill ? StackUtil.emptyStack : StackUtil.copyShrunk(input.fill, recipe.getInput().fill.getAmount())));
            }
        }
        return null;
    }
    
    @Override
    public Iterable<? extends MachineRecipe<Input, ItemStack>> getRecipes() {
        return this.recipes;
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
}
