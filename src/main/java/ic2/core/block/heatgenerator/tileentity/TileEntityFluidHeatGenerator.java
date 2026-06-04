package ic2.core.block.heatgenerator.tileentity;

import ic2.api.recipe.IFluidHeatManager;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.core.ContainerBase;
import ic2.core.FluidHeatManager;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityHeatSourceInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.heatgenerator.container.ContainerFluidHeatGenerator;
import ic2.core.block.heatgenerator.gui.GuiFluidHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.init.MainConfig;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.ConfigUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFluidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui {
  public final InvSlotConsumableLiquid fluidSlot = (InvSlotConsumableLiquid)new InvSlotConsumableLiquidByManager((IInventorySlotHolder)this, "fluidSlot", 1, (ILiquidAcceptManager)Recipes.fluidHeatGenerator);
  
  public final InvSlotOutput outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  @GuiSynced
  protected final FluidTank fluidTank = (FluidTank)this.fluids.addTankInsert("fluidTank", 10000, Fluids.fluidPredicate((ILiquidAcceptManager)Recipes.semiFluidGenerator));
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (needsFluid())
      needsInvUpdate = gainFuel(); 
    if (needsInvUpdate)
      func_70296_d(); 
    if (getActive() != this.newActive)
      setActive(this.newActive); 
  }
  
  public boolean isConverting() {
    return (getTankAmount() > 0 && this.HeatBuffer < getMaxHeatEmittedPerTick());
  }
  
  public static void init() {
    Recipes.fluidHeatGenerator = (IFluidHeatManager)new FluidHeatManager();
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidOil") > 0.0F)
      addFuel("oil", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidOil"))); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidFuel") > 0.0F)
      addFuel("fuel", 5, Math.round(768.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidFuel"))); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiomass") > 0.0F)
      addFuel("biomass", 20, Math.round(16.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiomass"))); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBioethanol") > 0.0F)
      addFuel("bio.ethanol", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBioethanol"))); 
    if (ConfigUtil.getFloat(MainConfig.get(), "balance/energy/generator/semiFluidBiogas") > 0.0F)
      addFuel("ic2biogas", 10, Math.round(32.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/semiFluidBiogas"))); 
  }
  
  public static void addFuel(String fluidName, int amount, int heat) {
    Recipes.fluidHeatGenerator.addFluid(fluidName, amount, heat);
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound) {
    super.readFromNBT(nbttagcompound);
    this.fluidTank.readFromNBT(nbttagcompound.getCompoundTag("fluidTank"));
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    NBTTagCompound fluidTankTag = new NBTTagCompound();
    this.fluidTank.writeToNBT(fluidTankTag);
    nbt.setTag("fluidTank", (NBTBase)fluidTankTag);
    return nbt;
  }
  
  protected int fillHeatBuffer(int maxAmount) {
    if (isConverting()) {
      if (this.ticker >= 19) {
        getFluidTank().drain(this.burnAmount, true);
        this.ticker = 0;
      } else {
        this.ticker = (short)(this.ticker + 1);
      } 
      this.newActive = true;
      return this.production;
    } 
    this.newActive = false;
    return 0;
  }
  
  public int getMaxHeatEmittedPerTick() {
    return calcHeatProduction();
  }
  
  public ContainerBase<TileEntityFluidHeatGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityFluidHeatGenerator>)new ContainerFluidHeatGenerator(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiFluidHeatGenerator(new ContainerFluidHeatGenerator(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  protected int calcHeatProduction() {
    if (this.fluidTank.getFluid() != null && 
      getFluidfromTank() != null) {
      IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(getFluidfromTank());
      if (property != null)
        return this.production = property.heat; 
    } 
    return this.production = 0;
  }
  
  protected void calcBurnAmount() {
    if (getFluidfromTank() != null) {
      IFluidHeatManager.BurnProperty property = Recipes.fluidHeatGenerator.getBurnProperty(getFluidfromTank());
      if (property != null) {
        this.burnAmount = property.amount;
        return;
      } 
    } 
    this.burnAmount = 0;
  }
  
  public FluidTank getFluidTank() {
    return this.fluidTank;
  }
  
  public FluidStack getFluidStackfromTank() {
    return getFluidTank().getFluid();
  }
  
  public Fluid getFluidfromTank() {
    return getFluidStackfromTank().getFluid();
  }
  
  public int getTankAmount() {
    return getFluidTank().getFluidAmount();
  }
  
  public int gaugeLiquidScaled(int i) {
    if (getFluidTank().getFluidAmount() <= 0)
      return 0; 
    return getFluidTank().getFluidAmount() * i / getFluidTank().getCapacity();
  }
  
  public boolean needsFluid() {
    return (getFluidTank().getFluidAmount() <= getFluidTank().getCapacity());
  }
  
  protected boolean gainFuel() {
    if (this.fluidTank.getFluid() != null) {
      calcHeatProduction();
      calcBurnAmount();
    } 
    return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.outputSlot);
  }
  
  private short ticker = 0;
  
  protected int burnAmount = 0;
  
  protected int production = 0;
  
  boolean newActive = false;
}
