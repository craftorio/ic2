// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import ic2.api.util.FluidContainerOutputMode;
import ic2.api.recipe.Recipes;
import net.minecraftforge.fluids.FluidTank;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IFillFluidContainerRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.util.StackUtil;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.item.ItemStack;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.machine.tileentity.TileEntityCanner;

public class InvSlotProcessableCanner extends InvSlotProcessable<Object, Object, Object>
{
    public InvSlotProcessableCanner(final TileEntityCanner base1, final String name1, final int count) {
        super(base1, name1, count, null);
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        switch (((TileEntityCanner)this.base).getMode()) {
            case BottleSolid:
            case EnrichLiquid: {
                return super.accepts(stack);
            }
            case BottleLiquid:
            case EmptyLiquid: {
                return false;
            }
            default: {
                assert false;
                return false;
            }
        }
    }
    
    @Override
    public void consume(final MachineRecipeResult<Object, Object, Object> result) {
        super.consume(result);
        final ItemStack containerStack = ((TileEntityCanner)this.base).canInputSlot.get();
        if (StackUtil.isEmpty(containerStack)) {
            ((TileEntityCanner)this.base).canInputSlot.clear();
        }
        final FluidStack fluid = ((TileEntityCanner)this.base).inputTank.getFluid();
        if (fluid != null && fluid.amount <= 0) {
            ((TileEntityCanner)this.base).inputTank.setFluid((FluidStack)null);
        }
    }
    
    @Override
    protected Object getInput(final ItemStack fill) {
        final ItemStack container = ((TileEntityCanner)this.base).canInputSlot.get();
        switch (((TileEntityCanner)this.base).getMode()) {
            case BottleSolid: {
                return new ICannerBottleRecipeManager.RawInput(container, fill);
            }
            case BottleLiquid: {
                return new IFillFluidContainerRecipeManager.Input(container, this.getTankFluid());
            }
            case EmptyLiquid: {
                return container;
            }
            case EnrichLiquid: {
                return new ICannerEnrichRecipeManager.RawInput(this.getTankFluid(), fill);
            }
            default: {
                assert false;
                return null;
            }
        }
    }
    
    @Override
    protected void setInput(final Object rawInput) {
        final InvSlotConsumableCanner canInputSlot = ((TileEntityCanner)this.base).canInputSlot;
        final FluidTank tank = ((TileEntityCanner)this.base).inputTank;
        switch (((TileEntityCanner)this.base).getMode()) {
            case BottleSolid: {
                final ICannerBottleRecipeManager.RawInput input = (ICannerBottleRecipeManager.RawInput)rawInput;
                canInputSlot.put(input.container);
                this.put(input.fill);
                break;
            }
            case BottleLiquid: {
                final IFillFluidContainerRecipeManager.Input input2 = (IFillFluidContainerRecipeManager.Input)rawInput;
                canInputSlot.put(input2.container);
                tank.drain((input2.fluid == null) ? tank.getFluidAmount() : (tank.getFluidAmount() - input2.fluid.amount), true);
                break;
            }
            case EmptyLiquid: {
                canInputSlot.put((ItemStack)rawInput);
                break;
            }
            case EnrichLiquid: {
                final ICannerEnrichRecipeManager.RawInput input3 = (ICannerEnrichRecipeManager.RawInput)rawInput;
                this.put(input3.additive);
                tank.drain((input3.fluid == null) ? tank.getFluidAmount() : (tank.getFluidAmount() - input3.fluid.amount), true);
                break;
            }
            default: {
                assert false;
                break;
            }
        }
    }
    
    @Override
    protected boolean allowEmptyInput() {
        return true;
    }
    
    @Override
    protected MachineRecipeResult<Object, Object, Object> getOutputFor(final Object input, final boolean forAccept) {
        return this.getOutput(input, forAccept);
    }
    
    protected MachineRecipeResult<Object, Object, Object> getOutput(final Object input, final boolean forAccept) {
        switch (((TileEntityCanner)this.base).getMode()) {
            case BottleSolid: {
                return (MachineRecipeResult<Object, Object, Object>)((IMachineRecipeManager<Object, Object, ICannerBottleRecipeManager.RawInput>)Recipes.cannerBottle).apply((ICannerBottleRecipeManager.RawInput)input, forAccept);
            }
            case BottleLiquid: {
                return (MachineRecipeResult<Object, Object, Object>)Recipes.fillFluidContainer.apply((IFillFluidContainerRecipeManager.Input)input, FluidContainerOutputMode.EmptyFullToOutput, forAccept);
            }
            case EmptyLiquid: {
                return (MachineRecipeResult<Object, Object, Object>)Recipes.emptyFluidContainer.apply((ItemStack)input, (this.getTankFluid() == null) ? null : this.getTankFluid().getFluid(), FluidContainerOutputMode.EmptyFullToOutput, forAccept);
            }
            case EnrichLiquid: {
                return (MachineRecipeResult<Object, Object, Object>)((IMachineRecipeManager<Object, Object, ICannerEnrichRecipeManager.RawInput>)Recipes.cannerEnrich).apply((ICannerEnrichRecipeManager.RawInput)input, forAccept);
            }
            default: {
                assert false;
                return null;
            }
        }
    }
    
    private FluidStack getTankFluid() {
        return ((TileEntityCanner)this.base).inputTank.getFluid();
    }
}
