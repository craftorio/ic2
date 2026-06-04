// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine;

import ic2.api.recipe.RecipeOutput;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.IRecipeInput;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import ic2.core.util.LiquidUtil;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;
import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.MachineRecipe;
import java.util.List;
import ic2.api.recipe.ICannerEnrichRecipeManager;

public class CannerEnrichRecipeManager implements ICannerEnrichRecipeManager
{
    private final List<MachineRecipe<Input, FluidStack>> recipes;
    
    public CannerEnrichRecipeManager() {
        this.recipes = new ArrayList<MachineRecipe<Input, FluidStack>>();
    }
    
    @Override
    public boolean addRecipe(final Input input, final FluidStack output, final NBTTagCompound metadata, final boolean replace) {
        if (input.fluid == null) {
            throw new NullPointerException("The fluid recipe input is null.");
        }
        if (input.additive == null) {
            throw new NullPointerException("The additive recipe input is null.");
        }
        if (output == null) {
            throw new NullPointerException("The recipe output is null.");
        }
        if (!LiquidUtil.check(input.fluid)) {
            throw new IllegalArgumentException("The fluid recipe input is invalid.");
        }
        if (!LiquidUtil.check(output)) {
            throw new IllegalArgumentException("The fluid recipe output is invalid.");
        }
        for (final ItemStack stack : input.additive.getInputs()) {
            final MachineRecipe<Input, FluidStack> recipe = this.getRecipe(input.fluid, stack, true);
            if (recipe != null) {
                if (!replace) {
                    return false;
                }
                this.recipes.remove(recipe);
            }
        }
        this.recipes.add(new MachineRecipe<Input, FluidStack>(input, output));
        return true;
    }
    
    @Override
    public void addRecipe(final FluidStack fluid, final IRecipeInput additive, final FluidStack output) {
        if (!this.addRecipe(new Input(fluid, additive), output, (NBTTagCompound)null, false)) {
            throw new RuntimeException("ambiguous recipe: [" + fluid + "+" + additive.getInputs() + " -> " + output + "]");
        }
    }
    
    @Override
    public MachineRecipeResult<Input, FluidStack, RawInput> apply(final RawInput input, final boolean acceptTest) {
        final MachineRecipe<Input, FluidStack> recipe = this.getRecipe(input.fluid, input.additive, acceptTest);
        if (recipe == null) {
            return null;
        }
        FluidStack remainingFluid;
        if (input.fluid == null) {
            remainingFluid = null;
        }
        else {
            final FluidStack copy;
            remainingFluid = (copy = input.fluid.copy());
            copy.amount -= recipe.getInput().fluid.amount;
            if (remainingFluid.amount <= 0) {
                remainingFluid = null;
            }
        }
        return recipe.getResult(new RawInput(remainingFluid, StackUtil.copyShrunk(input.additive, recipe.getInput().additive.getAmount())));
    }
    
    private MachineRecipe<Input, FluidStack> getRecipe(final FluidStack fluid, final ItemStack additive, final boolean acceptTest) {
        if (!acceptTest && (fluid == null || StackUtil.isEmpty(additive))) {
            return null;
        }
        for (final MachineRecipe<Input, FluidStack> recipe : this.recipes) {
            if ((fluid == null || (fluid.isFluidEqual(recipe.getInput().fluid) && (acceptTest || recipe.getInput().fluid.amount <= fluid.amount))) && (additive == null || (recipe.getInput().additive.matches(additive) && (acceptTest || recipe.getInput().additive.getAmount() <= StackUtil.getSize(additive))))) {
                return recipe;
            }
        }
        return null;
    }
    
    @Override
    public RecipeOutput getOutputFor(final FluidStack fluid, final ItemStack additive, final boolean adjustInput, final boolean acceptTest) {
        final MachineRecipeResult<Input, FluidStack, RawInput> result = this.apply(new RawInput(fluid, additive), acceptTest);
        if (result == null) {
            return null;
        }
        if (adjustInput) {
            fluid.amount = ((result.getAdjustedInput().fluid == null) ? 0 : result.getAdjustedInput().fluid.amount);
            additive.setCount(StackUtil.isEmpty(result.getAdjustedInput().additive) ? 0 : StackUtil.getSize(result.getAdjustedInput().additive));
        }
        final NBTTagCompound output = new NBTTagCompound();
        result.getOutput().writeToNBT(output);
        return new RecipeOutput(output, new ItemStack[0]);
    }
    
    @Override
    public Iterable<? extends MachineRecipe<Input, FluidStack>> getRecipes() {
        return this.recipes;
    }
    
    @Override
    public boolean isIterable() {
        return true;
    }
}
