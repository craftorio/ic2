package ic2.core.block.generator.tileentity;

import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.ISemiFluidFuelManager;
import ic2.api.recipe.Recipes;
import ic2.core.SemiFluidFuelManager;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;

@NotClassic
public class TileEntitySemifluidGenerator extends TileEntityBaseGenerator {
  public final InvSlotConsumableLiquid fluidSlot;
  
  public final InvSlotOutput outputSlot;
  
  @GuiSynced
  protected final FluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntitySemifluidGenerator() {
    super(32.0D, 1, 32000);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTankInsert("fluid", 10000, Fluids.fluidPredicate((ILiquidAcceptManager)Recipes.semiFluidGenerator));
    this.fluidSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByManager((IInventorySlotHolder)this, "fluidSlot", 1, (ILiquidAcceptManager)Recipes.semiFluidGenerator);
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  }
  
  public static void init() {
    Recipes.semiFluidGenerator = (ISemiFluidFuelManager)new SemiFluidFuelManager();
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidOil") > 0.0F)
      addFuel("oil", 16L, 8L); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidFuel") > 0.0F)
      addFuel("fuel", 128L, 32L); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiomass") > 0.0F)
      addFuel("biomass", 8L, 8L); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBioethanol") > 0.0F)
      addFuel("bio.ethanol", 128L, 32L); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiogas") > 0.0F)
      addFuel("ic2biogas", 32L, 16L); 
    addFuel("ic2creosote", 3L, 8L);
  }
  
  public static void addFuel(String fluidName, long energyPerMb, long energyPerTick) {
    Recipes.semiFluidGenerator.addFluid(fluidName, energyPerMb, energyPerTick);
  }
  
  public void updateEntityServer() {
    super.updateEntityServer();
    if (this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.outputSlot))
      func_70296_d(); 
  }
  
  public boolean gainEnergy() {
    if (isConverting()) {
      double temp = Math.min(this.fuel, this.production);
      this.energy.addEnergy(temp);
      this.fuel = (int)(this.fuel - temp);
      return true;
    } 
    return false;
  }
  
  public boolean needsFuel() {
    return (this.fuel < this.production && this.energy.getFreeEnergy() >= this.production);
  }
  
  public boolean gainFuel() {
    boolean dirty = false;
    FluidStack ret = this.fluidTank.drain(2147483647, false);
    if (ret != null) {
      ISemiFluidFuelManager.FuelProperty property = Recipes.semiFluidGenerator.getFuelProperty(ret.getFluid());
      int toBeConsumed = (property.energyPerMb >= property.energyPerTick) ? 1 : (int)Math.ceil(property.energyPerTick / property.energyPerMb);
      toBeConsumed = Math.min(toBeConsumed, ret.amount);
      if (property != null && ret.amount >= toBeConsumed) {
        this.fluidTank.drainInternal(toBeConsumed, true);
        this.production = property.energyPerTick;
        this.fuel = (int)(this.fuel + toBeConsumed * property.energyPerMb);
        dirty = true;
      } 
    } 
    return dirty;
  }
  
  public String getOperationSoundFile() {
    return "Generators/GeothermalLoop.ogg";
  }
}
