package ic2.core.block.machine.tileentity;

import ic2.api.energy.tile.IHeatSource;
import ic2.api.recipe.IFermenterRecipeManager;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByManager;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerFermenter;
import ic2.core.block.machine.gui.GuiFermenter;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CropResItemType;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.FermenterRecipeManager;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFermenter extends TileEntityInventory implements IHasGui, IGuiValueProvider, IUpgradableBlock {
  protected final Fluids fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
  
  private final FluidTank outputTank = (FluidTank)this.fluids.addTankExtract("output", 2000);
  
  private final FluidTank inputTank = (FluidTank)this.fluids.addTankInsert("input", 10000, Fluids.fluidPredicate((ILiquidAcceptManager)Recipes.fermenter));
  
  public final InvSlotOutput fluidInputCellOutSlot = new InvSlotOutput((IInventorySlotHolder)this, "biomassOutput", 1);
  
  public final InvSlotOutput fluidOutputCellOutSlot = new InvSlotOutput((IInventorySlotHolder)this, "biogassOutput", 1);
  
  public final InvSlotOutput fertiliserSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1);
  
  public final InvSlotUpgrade upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 2);
  
  public final InvSlotConsumableLiquidByTank fluidOutputCellInSlot = new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "biogasInput", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
  
  public final InvSlotConsumableLiquidByManager fluidInputCellInSlot = new InvSlotConsumableLiquidByManager((IInventorySlotHolder)this, "biomassInput", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (ILiquidAcceptManager)Recipes.fermenter);
  
  public static void init() {
    Recipes.fermenter = (IFermenterRecipeManager)new FermenterRecipeManager();
    Recipes.fermenter.addRecipe(FluidName.biomass.getName(), ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/need_amount_biomass_per_run"), 
        ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run"), FluidName.biogas.getName(), ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/output_amount_biogas_per_run"));
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound) {
    super.readFromNBT(nbttagcompound);
    this.inputTank.readFromNBT(nbttagcompound.getCompoundTag("inputTank"));
    this.outputTank.readFromNBT(nbttagcompound.getCompoundTag("outputTank"));
    this.progress = nbttagcompound.getInteger("progress");
    this.heatBuffer = nbttagcompound.getInteger("heatBuffer");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setTag("inputTank", (NBTBase)this.inputTank.writeToNBT(new NBTTagCompound()));
    nbt.setTag("outputTank", (NBTBase)this.outputTank.writeToNBT(new NBTTagCompound()));
    nbt.setInteger("progress", this.progress);
    nbt.setInteger("heatBuffer", this.heatBuffer);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.fluidInputCellInSlot.processIntoTank((IFluidTank)this.inputTank, this.fluidInputCellOutSlot);
    this.fluidOutputCellInSlot.processFromTank((IFluidTank)this.outputTank, this.fluidOutputCellOutSlot);
    this.newActive = work();
    if (getActive() != this.newActive)
      setActive(this.newActive); 
    this.upgradeSlot.tick();
  }
  
  private boolean work() {
    if (this.progress >= this.maxProgress) {
      this.fertiliserSlot.add(ItemName.crop_res.getItemStack((Enum)CropResItemType.fertilizer));
      this.progress = 0;
    } 
    EnumFacing dir = getFacing();
    TileEntity te = getWorld().getTileEntity(this.pos.offset(dir));
    if (te instanceof IHeatSource && this.inputTank.getFluid() != null) {
      IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluid().getFluid());
      if (fp != null && this.inputTank.getFluidAmount() >= fp.inputAmount && fp.outputAmount <= this.outputTank.getCapacity() - this.outputTank.getFluidAmount()) {
        this.heatBuffer += ((IHeatSource)te).drawHeat(dir.getOpposite(), 100, false);
        if (this.heatBuffer >= fp.heat) {
          this.heatBuffer -= fp.heat;
          this.inputTank.drainInternal(fp.inputAmount, true);
          this.outputTank.fillInternal(fp.getOutput(), true);
          this.progress += fp.inputAmount;
        } 
        return true;
      } 
    } 
    return false;
  }
  
  public ContainerBase<TileEntityFermenter> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityFermenter>)new ContainerFermenter(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiFermenter(new ContainerFermenter(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if ("heat".equals(name)) {
      if (this.heatBuffer == 0)
        return 0.0D; 
      double maxHeatBuff = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/hU_per_run");
      if (this.inputTank.getFluid() != null) {
        IFermenterRecipeManager.FermentationProperty fp = Recipes.fermenter.getFermentationInformation(this.inputTank.getFluid().getFluid());
        if (fp != null)
          maxHeatBuff = fp.heat; 
      } 
      return this.heatBuffer / maxHeatBuff;
    } 
    if ("progress".equals(name))
      return (this.progress == 0) ? 0.0D : (this.progress / this.maxProgress); 
    throw new IllegalArgumentException("Invalid GUI value: " + name);
  }
  
  public int gaugeLiquidScaled(int i, int tank) {
    switch (tank) {
      case 0:
        if (this.inputTank.getFluidAmount() <= 0)
          return 0; 
        return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
      case 1:
        if (this.outputTank.getFluidAmount() <= 0)
          return 0; 
        return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
    } 
    return 0;
  }
  
  public FluidTank getInputTank() {
    return this.inputTank;
  }
  
  public FluidTank getOutputTank() {
    return this.outputTank;
  }
  
  public double getEnergy() {
    return 40.0D;
  }
  
  public boolean useEnergy(double amount) {
    return true;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing);
  }
  
  private int heatBuffer = 0;
  
  public int progress = 0;
  
  private final int maxProgress = ConfigUtil.getInt(MainConfig.get(), "balance/fermenter/biomass_per_fertilizier");
  
  private boolean newActive = false;
}
