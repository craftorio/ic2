package ic2.core.block.invslot;

import ic2.api.util.FluidContainerOutputMode;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.apache.commons.lang3.mutable.MutableObject;

public class InvSlotConsumableLiquid extends InvSlotConsumable {
  private OpType opType;
  
  public InvSlotConsumableLiquid(IInventorySlotHolder<?> base1, String name1, int count) {
    this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP, OpType.Drain);
  }
  
  public InvSlotConsumableLiquid(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, OpType opType1) {
    super(base1, name1, access1, count, preferredSide1);
    this.opType = opType1;
  }
  
  public boolean accepts(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return false; 
    if (!LiquidUtil.isFluidContainer(stack))
      return false; 
    if (this.opType == OpType.Drain || this.opType == OpType.Both) {
      FluidStack containerFluid = null;
      if (stack.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null)) {
        ItemStack singleStack = StackUtil.copyWithSize(stack, 1);
        IFluidHandlerItem handler = (IFluidHandlerItem)singleStack.getCapability(CapabilityFluidHandler.FLUID_HANDLER_ITEM_CAPABILITY, null);
        if (handler != null)
          containerFluid = handler.drain(2147483647, false); 
      } 
      if (containerFluid != null && containerFluid.amount > 0 && 
        
        acceptsLiquid(containerFluid.getFluid()))
        return true; 
    } 
    if ((this.opType == OpType.Fill || this.opType == OpType.Both) && 
      LiquidUtil.isFillableFluidContainer(stack, getPossibleFluids()))
      return true; 
    return false;
  }
  
  public FluidStack drain(Fluid fluid, int maxAmount, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    if (fluid != null && !acceptsLiquid(fluid))
      return null; 
    if (this.opType != OpType.Drain && this.opType != OpType.Both)
      return null; 
    ItemStack stack = get();
    if (StackUtil.isEmpty(stack))
      return null; 
    LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(stack, fluid, maxAmount, FluidContainerOutputMode.EmptyFullToOutput);
    if (result == null)
      return null; 
    if (fluid == null && 
      !acceptsLiquid(result.fluidChange.getFluid()))
      return null; 
    output.setValue(result.extraOutput);
    if (!simulate)
      put(result.inPlaceOutput); 
    return result.fluidChange;
  }
  
  public int fill(FluidStack fs, MutableObject<ItemStack> output, boolean simulate) {
    output.setValue(null);
    if (fs == null || fs.amount <= 0)
      return 0; 
    if (this.opType != OpType.Fill && this.opType != OpType.Both)
      return 0; 
    ItemStack stack = get();
    if (StackUtil.isEmpty(stack))
      return 0; 
    LiquidUtil.FluidOperationResult result = LiquidUtil.fillContainer(stack, fs, FluidContainerOutputMode.EmptyFullToOutput);
    if (result == null)
      return 0; 
    output.setValue(result.extraOutput);
    if (!simulate)
      put(result.inPlaceOutput); 
    return result.fluidChange.amount;
  }
  
  public boolean transferToTank(IFluidTank tank, MutableObject<ItemStack> output, boolean simulate) {
    int space = tank.getCapacity();
    Fluid fluidRequired = null;
    FluidStack tankFluid = tank.getFluid();
    if (tankFluid != null) {
      space -= tankFluid.amount;
      fluidRequired = tankFluid.getFluid();
    } 
    FluidStack fluid = drain(fluidRequired, space, output, true);
    if (fluid == null)
      return false; 
    int amount = tank.fill(fluid, !simulate);
    if (amount <= 0)
      return false; 
    if (!simulate)
      drain(fluidRequired, amount, output, false); 
    return true;
  }
  
  public boolean transferFromTank(IFluidTank tank, MutableObject<ItemStack> output, boolean simulate) {
    FluidStack tankFluid = tank.drain(tank.getFluidAmount(), false);
    if (tankFluid == null || tankFluid.amount <= 0)
      return false; 
    int amount = fill(tankFluid, output, simulate);
    if (amount <= 0)
      return false; 
    if (!simulate)
      tank.drain(amount, true); 
    return true;
  }
  
  public boolean processIntoTank(IFluidTank tank, InvSlotOutput outputSlot) {
    if (isEmpty())
      return false; 
    MutableObject<ItemStack> output = new MutableObject();
    boolean wasChange = false;
    if (transferToTank(tank, output, true) && (StackUtil.isEmpty((ItemStack)output.getValue()) || outputSlot.canAdd((ItemStack)output.getValue()))) {
      wasChange = transferToTank(tank, output, false);
      if (!StackUtil.isEmpty((ItemStack)output.getValue()))
        outputSlot.add((ItemStack)output.getValue()); 
    } 
    return wasChange;
  }
  
  public boolean processFromTank(IFluidTank tank, InvSlotOutput outputSlot) {
    if (isEmpty() || tank.getFluidAmount() <= 0)
      return false; 
    MutableObject<ItemStack> output = new MutableObject();
    boolean wasChange = false;
    if (transferFromTank(tank, output, true) && (StackUtil.isEmpty((ItemStack)output.getValue()) || outputSlot.canAdd((ItemStack)output.getValue()))) {
      wasChange = transferFromTank(tank, output, false);
      if (!StackUtil.isEmpty((ItemStack)output.getValue()))
        outputSlot.add((ItemStack)output.getValue()); 
    } 
    return wasChange;
  }
  
  public void setOpType(OpType opType1) {
    this.opType = opType1;
  }
  
  protected boolean acceptsLiquid(Fluid fluid) {
    return true;
  }
  
  protected Iterable<Fluid> getPossibleFluids() {
    return null;
  }
  
  public enum OpType {
    Drain, Fill, Both, None;
  }
}
