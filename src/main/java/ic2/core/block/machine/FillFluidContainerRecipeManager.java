// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine;

import net.minecraftforge.fluids.FluidStack;
import ic2.api.recipe.MachineRecipe;
import java.util.Collections;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import ic2.api.util.FluidContainerOutputMode;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import java.util.Collection;
import ic2.api.recipe.IFillFluidContainerRecipeManager;

public class FillFluidContainerRecipeManager implements IFillFluidContainerRecipeManager
{
    @Override
    public boolean addRecipe(final Void input, final Collection<ItemStack> output, final NBTTagCompound metadata, final boolean replace) {
        return false;
    }
    
    @Override
    public MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(final Input input, final boolean acceptTest) {
        return this.apply(input, FluidContainerOutputMode.AnyToOutput, acceptTest);
    }
    
    @Override
    public MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(final Input input, final FluidContainerOutputMode outputMode, final boolean acceptTest) {
        if (StackUtil.isEmpty(input.container) || input.fluid == null) {
            if (!acceptTest) {
                return null;
            }
            if (StackUtil.isEmpty(input.container) && input.fluid == null) {
                return null;
            }
            if (StackUtil.isEmpty(input.container) || LiquidUtil.isFillableFluidContainer(input.container)) {
                return new MachineRecipe<Void, Collection<ItemStack>>(null, (Collection<ItemStack>)Collections.emptyList()).getResult(input);
            }
            return null;
        }
        else {
            if (input.fluid.amount <= 0) {
                return null;
            }
            final LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(input.container, input.fluid, outputMode);
            if (result == null) {
                return null;
            }
            final Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.emptyList() : Collections.singletonList(result.extraOutput);
            final FluidStack changedFluid = (result.fluidChange.amount >= input.fluid.amount) ? null : new FluidStack(input.fluid, input.fluid.amount - result.fluidChange.amount);
            return new MachineRecipe<Void, Collection<ItemStack>>(null, output).getResult(new Input(result.inPlaceOutput, changedFluid));
        }
    }
    
    @Override
    public Iterable<? extends MachineRecipe<Void, Collection<ItemStack>>> getRecipes() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isIterable() {
        return false;
    }
}
