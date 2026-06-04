package ic2.core.block.generator.tileentity;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergyTile;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.network.GuiSynced;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityConversionGenerator extends TileEntityInventory implements IHasGui, IEnergySource {
  private static final NumberFormat FORMAT = new DecimalFormat("#.#");
  
  @GuiSynced
  private double lastProduction;
  
  @GuiSynced
  private double maxProduction;
  
  private double production;
  
  private boolean registeredToEnet;
  
  protected void updateEntityServer() {
    super.updateEntityServer();
    this.lastProduction = this.production;
    this.production = 0.0D;
    setActive((this.maxProduction > 0.0D));
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if (this.registeredToEnet && !this.field_145850_b.isRemote) {
      EnergyNet.instance.removeTile((IEnergyTile)this);
      this.registeredToEnet = false;
    } 
  }
  
  protected void onLoaded() {
    super.onLoaded();
    if (!this.registeredToEnet && !this.field_145850_b.isRemote) {
      EnergyNet.instance.addTile((TileEntity)this);
      this.registeredToEnet = true;
    } 
  }
  
  public String getProduction() {
    return FORMAT.format(this.lastProduction);
  }
  
  public String getMaxProduction() {
    return FORMAT.format(this.maxProduction);
  }
  
  public ContainerBase<TileEntityConversionGenerator> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<TileEntityConversionGenerator>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  protected abstract int getEnergyAvailable();
  
  protected abstract void drawEnergyAvailable(int paramInt);
  
  protected abstract double getMultiplier();
  
  public double getOfferedEnergy() {
    return this.maxProduction = getEnergyAvailable() * getMultiplier();
  }
  
  public void drawEnergy(double amount) {
    this.production += amount;
    drawEnergyAvailable((int)Math.ceil(amount / getMultiplier()));
  }
  
  public int getSourceTier() {
    return Math.max(EnergyNet.instance.getTierFromPower(this.maxProduction), 2);
  }
  
  public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing side) {
    return (side != getFacing());
  }
}
