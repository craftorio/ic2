// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.recipe;

import net.minecraftforge.fluids.FluidStack;
import ic2.api.util.FluidContainerOutputMode;
import net.minecraft.item.ItemStack;
import java.util.Collection;

public interface IFillFluidContainerRecipeManager extends IMachineRecipeManager<Void, Collection<ItemStack>, Input>
{
    MachineRecipeResult<Void, Collection<ItemStack>, Input> apply(final Input p0, final FluidContainerOutputMode p1, final boolean p2);
    
    public static class Input
    {
        public final ItemStack container;
        public final FluidStack fluid;
        
        public Input(final ItemStack container, final FluidStack fluid) {
            this.container = container;
            this.fluid = fluid;
        }
    }
}
