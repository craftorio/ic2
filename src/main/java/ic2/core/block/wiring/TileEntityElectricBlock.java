package ic2.core.block.wiring;

import ic2.api.energy.EnergyNet;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.tile.IEnergyStorage;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.RedstoneEmitter;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.init.Localization;
import ic2.core.init.MainConfig;
import ic2.core.ref.TeBlock;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityElectricBlock extends TileEntityInventory implements IHasGui, INetworkClientTileEntityEventListener, IEnergyStorage {
  protected double output;
  
  public byte redstoneMode;
  
  public TileEntityElectricBlock(int tier, int output, int maxStorage) {
    this.redstoneMode = 0;
    this.output = output;
    this.chargeSlot = new InvSlotCharge((IInventorySlotHolder)this, tier);
    this.dischargeSlot = new InvSlotDischarge((IInventorySlotHolder)this, InvSlot.Access.IO, tier, InvSlot.InvSide.BOTTOM);
    this.energy = (Energy)addComponent((TileEntityComponent)(new Energy((TileEntityBlock)this, maxStorage, EnumSet.complementOf((EnumSet)EnumSet.of(EnumFacing.DOWN)), EnumSet.of(EnumFacing.DOWN), tier, tier, true)).addManagedSlot((InvSlot)this.chargeSlot).addManagedSlot((InvSlot)this.dischargeSlot));
    this.rsEmitter = (RedstoneEmitter)addComponent((TileEntityComponent)new RedstoneEmitter((TileEntityBlock)this));
    this.redstone = (Redstone)addComponent((TileEntityComponent)new Redstone((TileEntityBlock)this));
    this.comparator.setUpdate(this.energy::getComparatorValue);
  }
  
  public void readFromNBT(NBTTagCompound nbt) {
    superReadFromNBT(nbt);
    this.energy.setDirections(EnumSet.complementOf((EnumSet)EnumSet.of(getFacing())), EnumSet.of(getFacing()));
  }
  
  protected final void superReadFromNBT(NBTTagCompound nbt) {
    super.readFromNBT(nbt);
    this.redstoneMode = nbt.getByte("redstoneMode");
  }
  
  public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
    super.writeToNBT(nbt);
    nbt.setByte("redstoneMode", this.redstoneMode);
    return nbt;
  }
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.energy.setSendingEnabled(shouldEmitEnergy());
    this.rsEmitter.setLevel(shouldEmitRedstone() ? 15 : 0);
  }
  
  public ContainerBase<? extends TileEntityElectricBlock> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<? extends TileEntityElectricBlock>)new ContainerElectricBlock(player, this);
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)new GuiElectricBlock(new ContainerElectricBlock(player, this));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public void setFacing(EnumFacing facing) {
    super.setFacing(facing);
    this.energy.setDirections(EnumSet.complementOf((EnumSet)EnumSet.of(getFacing())), EnumSet.of(getFacing()));
  }
  
  protected final void superSetFacing(EnumFacing facing) {
    super.setFacing(facing);
  }
  
  protected boolean shouldEmitRedstone() {
    switch (this.redstoneMode) {
      case 1:
        return (this.energy.getEnergy() >= this.energy.getCapacity() - this.output * 20.0D);
      case 2:
        return (this.energy.getEnergy() > this.output && this.energy.getEnergy() < this.energy.getCapacity() - this.output);
      case 3:
        return (this.energy.getEnergy() < this.energy.getCapacity() - this.output);
      case 4:
        return (this.energy.getEnergy() < this.output);
    } 
    return false;
  }
  
  protected boolean shouldEmitEnergy() {
    boolean redstone = this.redstone.hasRedstoneInput();
    if (this.redstoneMode == 5)
      return !redstone; 
    if (this.redstoneMode == 6)
      return (!redstone || this.energy.getEnergy() > this.energy.getCapacity() - this.output * 20.0D); 
    return true;
  }
  
  public void onNetworkEvent(EntityPlayer player, int event) {
    this.redstoneMode = (byte)(this.redstoneMode + 1);
    if (this.redstoneMode >= redstoneModes)
      this.redstoneMode = 0; 
    IC2.platform.messagePlayer(player, getRedstoneMode(), new Object[0]);
  }
  
  public String getRedstoneMode() {
    if (this.redstoneMode >= redstoneModes || this.redstoneMode < 0)
      return ""; 
    return Localization.translate("ic2.EUStorage.gui.mod.redstone" + this.redstoneMode);
  }
  
  public void onPlaced(ItemStack stack, EntityLivingBase placer, EnumFacing facing) {
    super.onPlaced(stack, placer, facing);
    if (!(getWorld()).isRemote) {
      NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
      this.energy.addEnergy(nbt.getDouble("energy"));
    } 
  }
  
  public void onUpgraded() {
    rerender();
  }
  
  protected ItemStack adjustDrop(ItemStack drop, boolean wrench) {
    drop = super.adjustDrop(drop, wrench);
    if (wrench || this.teBlock.getDefaultDrop() == TeBlock.DefaultDrop.Self) {
      double retainedRatio = ConfigUtil.getDouble(MainConfig.get(), "balance/energyRetainedInStorageBlockDrops");
      double totalEnergy = this.energy.getEnergy();
      if (retainedRatio > 0.0D && totalEnergy > 0.0D) {
        NBTTagCompound nbt = StackUtil.getOrCreateNbtData(drop);
        nbt.setDouble("energy", totalEnergy * retainedRatio);
      } 
    } 
    return drop;
  }
  
  public int getOutput() {
    return (int)this.output;
  }
  
  public double getOutputEnergyUnitsPerTick() {
    return this.output;
  }
  
  public void setStored(int energy) {}
  
  public int addEnergy(int amount) {
    this.energy.addEnergy(amount);
    return amount;
  }
  
  public int getStored() {
    return (int)this.energy.getEnergy();
  }
  
  public int getCapacity() {
    return (int)this.energy.getCapacity();
  }
  
  public boolean isTeleporterCompatible(EnumFacing side) {
    return true;
  }
  
  @SideOnly(Side.CLIENT)
  public void addInformation(ItemStack stack, List<String> tooltip, ITooltipFlag advanced) {
    super.addInformation(stack, tooltip, advanced);
    tooltip.add(String.format("%s %.0f %s %s %d %s", new Object[] { Localization.translate("ic2.item.tooltip.Output"), Double.valueOf(EnergyNet.instance.getPowerFromTier(this.energy.getSourceTier())), Localization.translate("ic2.generic.text.EUt"), Localization.translate("ic2.item.tooltip.Capacity"), Integer.valueOf(getCapacity()), Localization.translate("ic2.generic.text.EU") }));
    tooltip.add(Localization.translate("ic2.item.tooltip.Store") + " " + (long)StackUtil.getOrCreateNbtData(stack).getDouble("energy") + " " + Localization.translate("ic2.generic.text.EU"));
  }
  
  public static byte redstoneModes = 7;
  
  public final InvSlotCharge chargeSlot;
  
  public final InvSlotDischarge dischargeSlot;
  
  public final Energy energy;
  
  public final Redstone redstone;
  
  public final RedstoneEmitter rsEmitter;
}
