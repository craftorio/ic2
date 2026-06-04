package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import ic2.core.util.LiquidUtil;
import ic2.core.util.PumpUtil;
import ic2.core.util.Util;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TileEntityPump extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider {
  public final int defaultTier;
  
  public int energyConsume;
  
  public int operationsPerTick;
  
  public final int defaultEnergyStorage;
  
  public final int defaultEnergyConsume;
  
  public final int defaultOperationLength;
  
  private AudioSource audioSource;
  
  private TileEntityMiner miner;
  
  public boolean redstonePowered;
  
  public final InvSlotConsumableLiquid containerSlot;
  
  public final InvSlotOutput outputSlot;
  
  public final InvSlotUpgrade upgradeSlot;
  
  @GuiSynced
  protected final FluidTank fluidTank;
  
  public short progress;
  
  public int operationLength;
  
  @GuiSynced
  public float guiProgress;
  
  protected final Fluids fluids;
  
  public TileEntityPump() {
    super(20, 1);
    this.miner = null;
    this.redstonePowered = false;
    this.progress = 0;
    this.containerSlot = new InvSlotConsumableLiquid((IInventorySlotHolder)this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
    this.outputSlot = new InvSlotOutput((IInventorySlotHolder)this, "output", 1, InvSlot.InvSide.SIDE);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgrade", 4);
    this.defaultEnergyConsume = this.energyConsume = 1;
    this.defaultOperationLength = this.operationLength = 20;
    this.defaultTier = 1;
    this.defaultEnergyStorage = 1 * this.operationLength;
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = (FluidTank)this.fluids.addTankExtract("fluid", 8000);
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(getWorld()).isRemote)
      setUpgradestat(); 
  }
  
  protected void onUnloaded() {
    if (IC2.platform.isRendering() && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
    this.miner = null;
    super.onUnloaded();
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.progress = nbt.getShort("progress");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.func_74777_a("progress", this.progress);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    boolean needsInvUpdate = false;
    if (canoperate() && this.energy.getEnergy() >= (this.energyConsume * this.operationLength)) {
      if (this.progress < this.operationLength) {
        this.progress = (short)(this.progress + 1);
        this.energy.useEnergy(this.energyConsume);
      } else {
        this.progress = 0;
        operate(false);
      } 
      setActive(true);
    } else {
      setActive(false);
    } 
    needsInvUpdate |= this.containerSlot.processFromTank((IFluidTank)this.fluidTank, this.outputSlot);
    needsInvUpdate |= this.upgradeSlot.tickNoMark();
    this.guiProgress = this.progress / this.operationLength;
    if (needsInvUpdate)
      super.markDirty(); 
  }
  
  public boolean canoperate() {
    return operate(true);
  }
  
  public boolean operate(boolean sim) {
    if (this.miner == null || this.miner.isInvalid()) {
      this.miner = null;
      World world = getWorld();
      for (EnumFacing dir : Util.downSideFacings) {
        TileEntity te = world.getTileEntity(this.pos.offset(dir));
        if (te instanceof TileEntityMiner) {
          this.miner = (TileEntityMiner)te;
          break;
        } 
      } 
    } 
    FluidStack liquid = null;
    if (this.miner != null) {
      if (this.miner.canProvideLiquid)
        liquid = pump(this.miner.liquidPos, sim, this.miner); 
    } else {
      EnumFacing dir = getFacing();
      liquid = pump(this.pos.offset(dir), sim, this.miner);
    } 
    if (liquid != null && this.fluidTank.fillInternal(liquid, false) > 0) {
      if (!sim)
        this.fluidTank.fillInternal(liquid, true); 
      return true;
    } 
    return false;
  }
  
  public FluidStack pump(BlockPos startPos, boolean sim, TileEntityMiner miner) {
    World world = getWorld();
    int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
    if (miner == null && freeSpace > 0) {
      TileEntity te = world.getTileEntity(startPos);
      EnumFacing side = getFacing().getOpposite();
      if (te != null && LiquidUtil.isFluidTile(te, side)) {
        if (freeSpace > 1000)
          freeSpace = 1000; 
        return LiquidUtil.drainTile(te, side, freeSpace, sim);
      } 
    } 
    if (freeSpace >= 1000) {
      BlockPos cPos;
      if (miner != null && miner.canProvideLiquid) {
        assert miner.liquidPos != null;
        cPos = miner.liquidPos;
      } else {
        cPos = PumpUtil.searchFluidSource(world, startPos);
      } 
      if (cPos != null)
        return LiquidUtil.drainBlock(world, cPos, sim); 
    } 
    return null;
  }
  
  public void markDirty() {
    super.markDirty();
    if (IC2.platform.isSimulating())
      setUpgradestat(); 
  }
  
  public void setUpgradestat() {
    double previousProgress = this.progress / this.operationLength;
    this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
    this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
    this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
    this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
    this.dischargeSlot.setTier(this.energy.getSinkTier());
    this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
    this.progress = (short)(int)Math.floor(previousProgress * this.operationLength + 0.1D);
  }
  
  public double getGuiValue(String name) {
    if (name.equals("progress"))
      return this.guiProgress; 
    throw new IllegalArgumentException(getClass().getSimpleName() + " Cannot get value for " + name);
  }
  
  public double getEnergy() {
    return this.energy.getEnergy();
  }
  
  public boolean useEnergy(double amount) {
    return this.energy.useEnergy(amount);
  }
  
  public ContainerBase<TileEntityPump> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityPump>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void onNetworkUpdate(String field) {
    if (field.equals("active")) {
      if (this.audioSource == null)
        this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/PumpOp.ogg", true, false, IC2.audioManager.getDefaultVolume()); 
      if (getActive()) {
        if (this.audioSource != null)
          this.audioSource.play(); 
      } else if (this.audioSource != null) {
        this.audioSource.stop();
      } 
    } 
    super.onNetworkUpdate(field);
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.Processing, new UpgradableProperty[] { UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing });
  }
}
