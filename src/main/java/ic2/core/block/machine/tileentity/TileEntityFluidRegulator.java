package ic2.core.block.machine.tileentity;

import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Fluids;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.machine.container.ContainerFluidRegulator;
import ic2.core.block.machine.gui.GuiFluidRegulator;
import ic2.core.init.Localization;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.util.LiquidUtil;
import java.util.Collections;
import java.util.EnumSet;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityFluidRegulator extends TileEntityElectricMachine implements IHasGui, INetworkClientTileEntityEventListener {
  private int mode;
  
  private int updateTicker;
  
  private int outputmb;
  
  private boolean newActive;
  
  public final InvSlotOutput wasseroutputSlot;
  
  public final InvSlotConsumableLiquidByTank wasserinputSlot;
  
  @GuiSynced
  protected final Fluids.InternalFluidTank fluidTank;
  
  protected final Fluids fluids;
  
  public TileEntityFluidRegulator() {
    super(10000, 4);
    this.fluids = (Fluids)addComponent((TileEntityComponent)new Fluids((TileEntityBlock)this));
    this.fluidTank = this.fluids.addTank("fluidTank", 10000, InvSlot.Access.NONE);
    this.wasserinputSlot = new InvSlotConsumableLiquidByTank((IInventorySlotHolder)this, "wasserinputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Drain, (IFluidTank)this.fluidTank);
    this.wasseroutputSlot = new InvSlotOutput((IInventorySlotHolder)this, "wasseroutputSlot", 1);
    this.newActive = false;
    this.outputmb = 0;
    this.mode = 0;
    this.updateTicker = IC2.random.nextInt(getTickRate());
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.outputmb = nbt.getInteger("outputmb");
    this.mode = nbt.getInteger("mode");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setInteger("outputmb", this.outputmb);
    nbt.setInteger("mode", this.mode);
    return nbt;
  }
  
  protected void onLoaded() {
    super.onLoaded();
    updateConnectivity();
  }
  
  public void setFacing(EnumFacing side) {
    super.setFacing(side);
    updateConnectivity();
  }
  
  private void updateConnectivity() {
    this.fluids.changeConnectivity(this.fluidTank, EnumSet.complementOf((EnumSet)EnumSet.of(getFacing())), Collections.emptySet());
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.wasserinputSlot.processIntoTank((IFluidTank)this.fluidTank, this.wasseroutputSlot);
    if (this.updateTicker++ % getTickRate() != 0 && this.mode == 0)
      return; 
    this.newActive = work();
    if (getActive() != this.newActive)
      setActive(this.newActive); 
  }
  
  private boolean work() {
    if (this.outputmb == 0)
      return false; 
    if (this.energy.getEnergy() < 10.0D)
      return false; 
    if (this.fluidTank.getFluidAmount() <= 0)
      return false; 
    EnumFacing dir = getFacing();
    TileEntity te = getWorld().getTileEntity(this.pos.offset(dir));
    EnumFacing side = dir.getOpposite();
    if (LiquidUtil.isFluidTile(te, side)) {
      int amount = LiquidUtil.fillTile(te, side, this.fluidTank.drainInternal(this.outputmb, false), false);
      if (amount > 0) {
        this.fluidTank.drainInternal(this.outputmb, true);
        this.energy.useEnergy(10.0D);
        return true;
      } 
    } 
    return false;
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    if (event == 1001 || event == 1002) {
      if (event == 1001 && this.mode == 0)
        this.mode = 1; 
      if (event == 1002 && this.mode == 1)
        this.mode = 0; 
      return;
    } 
    this.outputmb += event;
    if (this.outputmb > 1000)
      this.outputmb = 1000; 
    if (this.outputmb < 0)
      this.outputmb = 0; 
  }
  
  public int getTickRate() {
    return 20;
  }
  
  public ContainerBase<TileEntityFluidRegulator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityFluidRegulator>)new ContainerFluidRegulator(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiFluidRegulator(new ContainerFluidRegulator(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public int gaugeLiquidScaled(int i, int tank) {
    switch (tank) {
      case 0:
        if (this.fluidTank.getFluidAmount() <= 0)
          return 0; 
        return this.fluidTank.getFluidAmount() * i / this.fluidTank.getCapacity();
    } 
    return 0;
  }
  
  public int getoutputmb() {
    return this.outputmb;
  }
  
  public String getmodegui() {
    switch (this.mode) {
      case 0:
        return Localization.translate("ic2.generic.text.sec");
      case 1:
        return Localization.translate("ic2.generic.text.tick");
    } 
    return "";
  }
  
  public FluidTank getFluidTank() {
    return (FluidTank)this.fluidTank;
  }
}
