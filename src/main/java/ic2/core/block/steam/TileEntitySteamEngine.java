package ic2.core.block.steam;

import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.ref.FluidName;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.Fluid;

public class TileEntitySteamEngine extends TileEntityInventory implements IKineticProvider {
  protected int ticksSinceLastActiveUpdate = IC2.random.nextInt(128);
  
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  protected final Fluids.InternalFluidTank fluidTank = this.fluids.addTankInsert("steam", 1000, InvSlot.InvSide.ANY, Fluids.fluidPredicate(new Fluid[] { FluidName.biomass.getInstance() }));
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.delta = nbt.func_74762_e("delta");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74768_a("delta", this.delta);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInventoryUpdate = false;
    boolean newActive = work();
    if (needsInventoryUpdate)
      func_70296_d(); 
    if (!delayActiveUpdate()) {
      setActive(newActive);
    } else {
      if (this.ticksSinceLastActiveUpdate % 128 == 0) {
        setActive((this.activityMeter > 0));
        this.activityMeter = 0;
      } 
      if (newActive) {
        this.activityMeter++;
      } else {
        this.activityMeter--;
      } 
      this.ticksSinceLastActiveUpdate++;
    } 
  }
  
  public boolean work() {
    if (this.fluidTank.getFluidAmount() > 1) {
      this.fluidTank.drainInternal(1, true);
      this.delta = Math.min(++this.delta, 200);
      this.power = (int)(getMaxPower() / 10.0D * (this.delta / 20));
      return true;
    } 
    this.delta = Math.max(--this.delta, 0);
    this.power = (int)(getMaxPower() / 10.0D * (this.delta / 20));
    return false;
  }
  
  public boolean delayActiveUpdate() {
    return false;
  }
  
  public int getProvidedPower(EnumFacing side) {
    return (side == getFacing()) ? this.power : 0;
  }
  
  public int getMaxPower() {
    return 4;
  }
  
  protected int power = 0;
  
  protected int delta = 0;
  
  protected int activityMeter = 0;
}
