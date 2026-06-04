// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraftforge.fluids.IFluidTank;
import ic2.api.util.FluidContainerOutputMode;
import org.apache.commons.lang3.mutable.MutableObject;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public class InvSlotConsumableLiquid extends InvSlotConsumable
{
    private OpType opType;
    
    public InvSlotConsumableLiquid(final IInventorySlotHolder<?> base1, final String name1, final int count) {
        this(base1, name1, Access.I, count, InvSide.TOP, OpType.Drain);
    }
    
    public InvSlotConsumableLiquid(final IInventorySlotHolder<?> base1, final String name1, final Access access1, final int count, final InvSide preferredSide1, final OpType opType1) {
        super(base1, name1, access1, count, preferredSide1);
        this.opType = opType1;
    }
    
    @Override
    public boolean accepts(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        if (!LiquidUtil.isFluidContainer(stack)) {
            return false;
        }
        if (this.opType == OpType.Drain || this.opType == OpType.Both) {
            FluidStack containerFluid = null;
            if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null)) {
                final ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
                final IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, (EnumFacing)null);
                if (handler != null) {
                    containerFluid = handler.drain(Integer.MAX_VALUE, false);
                }
            }
            if (containerFluid != null && containerFluid.amount > 0 && this.acceptsLiquid(containerFluid.getFluid())) {
                return true;
            }
        }
        return (this.opType == OpType.Fill || this.opType == OpType.Both) && LiquidUtil.isFillableFluidContainer(stack, this.getPossibleFluids());
    }
    
    public FluidStack drain(final Fluid fluid, final int maxAmount, final MutableObject<ItemStack> output, final boolean simulate) {
        output.setValue((Object)null);
        if (fluid != null && !this.acceptsLiquid(fluid)) {
            return null;
        }
        if (this.opType != OpType.Drain && this.opType != OpType.Both) {
            return null;
        }
        final ItemStack stack = this.get();
        if (StackUtil.isEmpty(stack)) {
            return null;
        }
        final LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(stack, fluid, maxAmount, FluidContainerOutputMode.EmptyFullToOutput);
        if (result == null) {
            return null;
        }
        if (fluid == null && !this.acceptsLiquid(result.fluidChange.getFluid())) {
            return null;
        }
        output.setValue((Object)result.extraOutput);
        if (!simulate) {
            this.put(result.inPlaceOutput);
        }
        return result.fluidChange;
    }
    
    public int fill(final FluidStack fs, final MutableObject<ItemStack> output, final boolean simulate) {
        output.setValue((Object)null);
        if (fs == null || fs.amount <= 0) {
            return 0;
        }
        if (this.opType != OpType.Fill && this.opType != OpType.Both) {
            return 0;
        }
        final ItemStack stack = this.get();
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        final LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(stack, fs, FluidContainerOutputMode.EmptyFullToOutput);
        if (result == null) {
            return 0;
        }
        output.setValue((Object)result.extraOutput);
        if (!simulate) {
            this.put(result.inPlaceOutput);
        }
        return result.fluidChange.amount;
    }
    
    public boolean transferToTank(final IFluidTank tank, final MutableObject<ItemStack> output, final boolean simulate) {
        int space = tank.getCapacity();
        Fluid fluidRequired = null;
        final FluidStack tankFluid = tank.getFluid();
        if (tankFluid != null) {
            space -= tankFluid.amount;
            fluidRequired = tankFluid.getFluid();
        }
        final FluidStack fluid = this.drain(fluidRequired, space, output, true);
        if (fluid == null) {
            return false;
        }
        final int amount = tank.fill(fluid, !simulate);
        if (amount <= 0) {
            return false;
        }
        if (!simulate) {
            this.drain(fluidRequired, amount, output, false);
        }
        return true;
    }
    
    public boolean transferFromTank(final IFluidTank tank, final MutableObject<ItemStack> output, final boolean simulate) {
        final FluidStack tankFluid = tank.drain(tank.getFluidAmount(), false);
        if (tankFluid == null || tankFluid.amount <= 0) {
            return false;
        }
        final int amount = this.fill(tankFluid, output, simulate);
        if (amount <= 0) {
            return false;
        }
        if (!simulate) {
            tank.drain(amount, true);
        }
        return true;
    }
    
    public boolean processIntoTank(final IFluidTank tank, final InvSlotOutput outputSlot) {
        if (this.isEmpty()) {
            return false;
        }
        final MutableObject<ItemStack> output = (MutableObject<ItemStack>)new MutableObject();
        boolean wasChange = false;
        if (this.transferToTank(tank, output, true) && (StackUtil.isEmpty((ItemStack)output.getValue()) || outputSlot.canAdd((ItemStack)output.getValue()))) {
            wasChange = this.transferToTank(tank, output, false);
            if (!StackUtil.isEmpty((ItemStack)output.getValue())) {
                outputSlot.add((ItemStack)output.getValue());
            }
        }
        return wasChange;
    }
    
    public boolean processFromTank(final IFluidTank tank, final InvSlotOutput outputSlot) {
        if (this.isEmpty() || tank.getFluidAmount() <= 0) {
            return false;
        }
        final MutableObject<ItemStack> output = (MutableObject<ItemStack>)new MutableObject();
        boolean wasChange = false;
        if (this.transferFromTank(tank, output, true) && (StackUtil.isEmpty((ItemStack)output.getValue()) || outputSlot.canAdd((ItemStack)output.getValue()))) {
            wasChange = this.transferFromTank(tank, output, false);
            if (!StackUtil.isEmpty((ItemStack)output.getValue())) {
                outputSlot.add((ItemStack)output.getValue());
            }
        }
        return wasChange;
    }
    
    public void setOpType(final OpType opType1) {
        this.opType = opType1;
    }
    
    protected boolean acceptsLiquid(final Fluid fluid) {
        return true;
    }
    
    protected Iterable<Fluid> getPossibleFluids() {
        return null;
    }
    
    public enum OpType
    {
        Drain, 
        Fill, 
        Both, 
        None;
    }
}
