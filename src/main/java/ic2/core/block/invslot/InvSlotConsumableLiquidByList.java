package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraftforge.fluids.Fluid;

public class InvSlotConsumableLiquidByList extends InvSlotConsumableLiquid {
  private final Set<Fluid> acceptedFluids;
  
  public InvSlotConsumableLiquidByList(IInventorySlotHolder<?> base1, String name1, int count, Fluid... fluidlist) {
    super(base1, name1, count);
    this.acceptedFluids = new HashSet<>(Arrays.asList(fluidlist));
  }
  
  public InvSlotConsumableLiquidByList(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, InvSlotConsumableLiquid.OpType opType, Fluid... fluidlist) {
    super(base1, name1, access1, count, preferredSide1, opType);
    this.acceptedFluids = new HashSet<>(Arrays.asList(fluidlist));
  }
  
  protected boolean acceptsLiquid(Fluid fluid) {
    return this.acceptedFluids.contains(fluid);
  }
  
  protected Iterable<Fluid> getPossibleFluids() {
    return this.acceptedFluids;
  }
}
