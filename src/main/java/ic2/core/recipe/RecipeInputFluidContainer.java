// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.recipe;

import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import java.util.Iterator;
import java.util.Collections;
import ic2.core.IC2;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import java.util.ArrayList;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.LoaderState;
import java.util.List;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import ic2.api.recipe.IRecipeInput;

public class RecipeInputFluidContainer extends RecipeInputBase implements IRecipeInput
{
    private final Fluid fluid;
    private final int amount;
    private static volatile FluidHandlerInfo fluidHandlerInfo;
    
    RecipeInputFluidContainer(final Fluid fluid) {
        this(fluid, 1000);
    }
    
    RecipeInputFluidContainer(final Fluid fluid, final int amount) {
        this.fluid = fluid;
        this.amount = amount;
    }
    
    @Override
    public boolean matches(final ItemStack subject) {
        final FluidStack fs = FluidUtil.getFluidContained(subject);
        return (fs == null && this.fluid == null) || (fs != null && fs.getFluid() == this.fluid && fs.amount >= this.amount);
    }
    
    @Override
    public int getAmount() {
        return 1;
    }
    
    @Override
    public List<ItemStack> getInputs() {
        return getFluidContainer(this.fluid);
    }
    
    public String toString() {
        return "RInputFluidContainer<" + this.amount + "x" + this.fluid.getName() + ">";
    }
    
    public boolean equals(final Object obj) {
        final RecipeInputFluidContainer other;
        return obj != null && this.getClass() == obj.getClass() && (other = (RecipeInputFluidContainer)obj).fluid == this.fluid && other.amount == this.amount;
    }
    
    public static List<ItemStack> getFluidContainer(final Fluid fluid) {
        FluidHandlerInfo info = RecipeInputFluidContainer.fluidHandlerInfo;
        if (info.loaderState != LoaderState.AVAILABLE && info.loaderState != Loader.instance().getLoaderState()) {
            final List<ItemStack> list = new ArrayList<ItemStack>();
            for (final Item item : ForgeRegistries.ITEMS) {
                final ItemStack stack = new ItemStack(item);
                final IFluidHandlerItem handler = FluidUtil.getFluidHandler(stack);
                if (handler != null) {
                    handler.drain(Integer.MAX_VALUE, true);
                    final ItemStack container = handler.getContainer();
                    if (container == null) {
                        IC2.platform.displayError("Null container stack!\nItem: %s\nRegistry: %s\nUnlocalised: %s\nHandler: %s (%s)", item, item.getRegistryName(), item.getUnlocalizedName(), handler, handler.getClass());
                    }
                    if (FluidUtil.getFluidContained(container) != null) {
                        continue;
                    }
                    list.add(stack);
                }
            }
            final LoaderState state = Loader.instance().hasReachedState(LoaderState.AVAILABLE) ? LoaderState.AVAILABLE : Loader.instance().getLoaderState();
            info = (RecipeInputFluidContainer.fluidHandlerInfo = new FluidHandlerInfo(Collections.unmodifiableList((List<? extends ItemStack>)list), state));
        }
        if (fluid == null) {
            return info.items;
        }
        final List<ItemStack> ret = new ArrayList<ItemStack>();
        for (final ItemStack stack2 : info.items) {
            final IFluidHandlerItem handler2 = FluidUtil.getFluidHandler(stack2.copy());
            if (handler2 != null && handler2.fill(new FluidStack(fluid, Integer.MAX_VALUE), true) > 0) {
                final ItemStack container2 = handler2.getContainer();
                if (FluidUtil.getFluidContained(container2) == null) {
                    continue;
                }
                ret.add(container2);
            }
        }
        return ret;
    }
    
    static {
        RecipeInputFluidContainer.fluidHandlerInfo = new FluidHandlerInfo(Collections.emptyList(), LoaderState.PREINITIALIZATION);
    }
    
    private static class FluidHandlerInfo
    {
        final List<ItemStack> items;
        final LoaderState loaderState;
        
        FluidHandlerInfo(final List<ItemStack> items, final LoaderState loaderState) {
            this.items = items;
            this.loaderState = loaderState;
        }
    }
}
