package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.ILiquidAcceptManager;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerElectrolyzer;
import ic2.core.block.machine.gui.GuiElectrolyzer;
import ic2.core.gui.CustomGauge;
import ic2.core.recipe.ElectrolyzerRecipeManager;
import ic2.core.ref.FluidName;
import ic2.core.ref.TeBlock.Delegated;
import ic2.core.util.LiquidUtil;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@Delegated(current = TileEntityElectrolyzer.class, old = TileEntityClassicElectrolyzer.class)
public class TileEntityElectrolyzer extends TileEntityElectricMachine implements IUpgradableBlock, IHasGui, CustomGauge.IGaugeRatioProvider {
  protected int progress;
  
  protected IElectrolyzerRecipeManager.ElectrolyzerRecipe recipe;
  
  protected FluidTank input;
  
  public final InvSlotUpgrade upgradeSlot;
  
  protected final Fluids fluids;
  
  public static Class<? extends TileEntityInventory> delegate() {
    return IC2.version.isClassic() ? (Class)TileEntityClassicElectrolyzer.class : (Class)TileEntityElectrolyzer.class;
  }
  
  public TileEntityElectrolyzer() {
    super(32000, 2);
    this.progress = 0;
    this.recipe = null;
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.input = (FluidTank)this.fluids.addTankInsert("input", 8000, Fluids.fluidPredicate((ILiquidAcceptManager)Recipes.electrolyzer));
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgradeSlot", 4);
  }
  
  public static void init() {
    Recipes.electrolyzer = (IElectrolyzerRecipeManager)new ElectrolyzerRecipeManager();
    Recipes.electrolyzer.addRecipe(FluidRegistry.WATER.getName(), 40, 32, new IElectrolyzerRecipeManager.ElectrolyzerOutput[] { new IElectrolyzerRecipeManager.ElectrolyzerOutput(FluidName.hydrogen.getName(), 26, EnumFacing.DOWN), new IElectrolyzerRecipeManager.ElectrolyzerOutput(FluidName.oxygen.getName(), 13, EnumFacing.UP) });
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.progress = nbt.getInteger("progress");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("progress", this.progress);
    return nbt;
  }
  
  public void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (canOperate()) {
      assert this.recipe != null;
      setActive(true);
      this.energy.useEnergy(this.recipe.EUaTick);
      this.progress++;
      if (this.progress >= this.recipe.ticksNeeded) {
        operate();
        this.progress = 0;
        needsInvUpdate = true;
      } 
    } else {
      setActive(false);
      this.progress = 0;
    } 
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    if (needsInvUpdate)
      markDirty(); 
  }
  
  protected boolean canOperate() {
    if (this.input.getFluid() == null)
      return false; 
    this.recipe = Recipes.electrolyzer.getElectrolysisInformation(this.input.getFluid().getFluid());
    if (this.recipe == null || this.energy.getEnergy() < this.recipe.EUaTick || this.input.getFluidAmount() < this.recipe.inputAmount)
      return false; 
    for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs) {
      if (!canFillTank(output.tankDirection, output.getOutput()))
        return false; 
    } 
    return true;
  }
  
  protected void operate() {
    assert this.recipe != null;
    this.input.drainInternal(this.recipe.inputAmount, true);
    for (IElectrolyzerRecipeManager.ElectrolyzerOutput output : this.recipe.outputs)
      fillTank(output.tankDirection, output.getOutput()); 
  }
  
  protected boolean canFillTank(EnumFacing facing, FluidStack fluid) {
    TileEntity te = getWorld().getTileEntity(this.pos.offset(facing));
    if (te instanceof TileEntityTank)
      return (LiquidUtil.fillTile(te, facing, fluid, true) == fluid.amount); 
    return false;
  }
  
  protected void fillTank(EnumFacing facing, FluidStack fluid) {
    TileEntity te = getWorld().getTileEntity(this.pos.offset(facing));
    if (te instanceof TileEntityTank)
      LiquidUtil.fillTile(te, facing, fluid, false); 
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.FluidConsuming);
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public ContainerBase<TileEntityElectrolyzer> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityElectrolyzer>)new ContainerElectrolyzer(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiElectrolyzer(new ContainerElectrolyzer(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public FluidTank getInput() {
    return this.input;
  }
  
  public boolean hasRecipe() {
    return (getCurrentRecipe() != null);
  }
  
  public IElectrolyzerRecipeManager.ElectrolyzerRecipe getCurrentRecipe() {
    return this.recipe;
  }
  
  public double getRatio() {
    return (this.recipe == null) ? 0.0D : (this.progress / this.recipe.ticksNeeded);
  }
}
