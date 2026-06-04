// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine;

import java.util.Collection;
import ic2.api.recipe.MachineRecipe;
import java.util.Collections;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraftforge.fluids.Fluid;
import ic2.api.util.FluidContainerOutputMode;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.recipe.IEmptyFluidContainerRecipeManager;

public class EmptyFluidContainerRecipeManager implements IEmptyFluidContainerRecipeManager
{
    @Override
    public boolean addRecipe(final Void input, final Output output, final NBTTagCompound metadata, final boolean replace) {
        return false;
    }
    
    @Override
    public MachineRecipeResult<Void, Output, ItemStack> apply(final ItemStack input, final boolean acceptTest) {
        return this.apply(input, null, FluidContainerOutputMode.AnyToOutput, acceptTest);
    }
    
    @Override
    public MachineRecipeResult<Void, Output, ItemStack> apply(final ItemStack input, final Fluid requiredFluid, final FluidContainerOutputMode outputMode, final boolean acceptTest) {
        if (StackUtil.isEmpty(input)) {
            return null;
        }
        final LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(input, requiredFluid, Integer.MAX_VALUE, outputMode);
        if (result == null) {
            return null;
        }
        final Collection<ItemStack> output = StackUtil.isEmpty(result.extraOutput) ? Collections.emptyList() : Collections.singletonList(result.extraOutput);
        return new MachineRecipe<Void, Output>(null, new Output(output, result.fluidChange)).getResult(result.inPlaceOutput);
    }
    
    @Override
    public Iterable<? extends MachineRecipe<Void, Output>> getRecipes() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isIterable() {
        return false;
    }
}
