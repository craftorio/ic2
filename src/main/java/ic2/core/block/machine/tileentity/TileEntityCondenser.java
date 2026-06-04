package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.block.machine.gui.GuiCondenser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import java.util.EnumSet;
import java.util.Set;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityCondenser extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, IUpgradableBlock {
  private final short passiveCooling = 100;
  
  private final short coolingPerVent = 100;
  
  public final short ventEUCost = 2;
  
  public int progress;
  
  public final int maxProgress = 10000;
  
  private final FluidTank inputTank;
  
  private final FluidTank outputTank;
  
  public final InvSlotConsumableLiquidByTank waterInputSlot;
  
  public final InvSlotOutput wateroutputSlot;
  
  public final InvSlotConsumableId ventSlots;
  
  public final InvSlotUpgrade upgradeSlot;
  
  protected final Fluids fluids;
  
  public TileEntityCondenser() {
    super(100000, 3);
    this.passiveCooling = 100;
    this.coolingPerVent = 100;
    this.ventEUCost = 2;
    this.progress = 0;
    this.maxProgress = 10000;
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.inputTank = (FluidTank)this.fluids.addTankInsert("inputTank", 100000, Fluids.fluidPredicate(new Fluid[] { FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance() }));
    this.outputTank = (FluidTank)this.fluids.addTankExtract("outputTank", 1000);
    this.waterInputSlot = new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "waterInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, (IFluidTank)this.outputTank);
    this.wateroutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "wateroutputSlot", 1);
    this.ventSlots = new InvSlotConsumableId((IInventorySlotHolder)this, "ventSlots", 4, new Item[] { ItemName.heat_vent.getInstance() });
    this.ventSlots.setStackSizeLimit(1);
    this.upgradeSlot = new InvSlotUpgrade((IInventorySlotHolder)this, "upgradeSlot", 1);
  }
  
  public void readFromNBT(NBTTagCompound nbttagcompound) {
    super.readFromNBT(nbttagcompound);
    this.progress = nbttagcompound.getInteger("progress");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("progress", this.progress);
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!(getWorld()).isRemote)
      updateTier(); 
  }
  
  public byte getVents() {
    byte vents = 0;
    for (int slot = 0; slot < this.ventSlots.size(); slot++) {
      if (!this.ventSlots.isEmpty(slot))
        vents = (byte)(vents + 1); 
    } 
    return vents;
  }
  
  public void markDirty() {
    super.markDirty();
    if (!(getWorld()).isRemote)
      updateTier(); 
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.waterInputSlot.processFromTank((IFluidTank)this.outputTank, this.wateroutputSlot);
    setActive((this.inputTank.getFluidAmount() > 0));
    work();
    if (this.upgradeSlot.tickNoMark())
      super.markDirty(); 
  }
  
  private void work() {
    if (this.outputTank.getCapacity() - this.outputTank.getFluidAmount() >= 100) {
      if (this.progress >= 10000) {
        this.outputTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 100), true);
        this.progress -= 10000;
      } 
      if (this.inputTank.getFluidAmount() > 0) {
        byte vents = getVents();
        int drain = 100 + vents * 100;
        if (this.energy.useEnergy((vents * 2)))
          this.progress += (this.inputTank.drainInternal(drain, true)).amount; 
      } 
    } 
  }
  
  private void updateTier() {
    this.upgradeSlot.onChanged();
    int tier = this.upgradeSlot.getTier(3);
    this.energy.setSinkTier(tier);
    this.dischargeSlot.setTier(tier);
  }
  
  public ContainerBase<TileEntityCondenser> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityCondenser>)new ContainerCondenser(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiCondenser(new ContainerCondenser(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    if ("progress".equals(name))
      return (this.progress == 0) ? 0.0D : (this.progress / 10000.0D); 
    throw new IllegalArgumentException("Invalid Gui value: " + name);
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
    return 0.0D;
  }
  
  public boolean useEnergy(double amount) {
    return false;
  }
  
  public Set<UpgradableProperty> getUpgradableProperties() {
    return EnumSet.of(UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming, UpgradableProperty.FluidProducing, UpgradableProperty.Transformer);
  }
}
