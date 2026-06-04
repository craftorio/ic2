package ic2.core.block.steam;

import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.audio.AudioSource;
import ic2.core.audio.PositionSpec;
import ic2.core.block.TileEntityInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.DynamicGui;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GuiSynced;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class TileEntityKineticMachine extends TileEntityInventory implements IKineticMachine, IHasGui, IGuiValueProvider, INetworkTileEntityEventListener {
  protected final int minimumPowerRequired;
  
  protected final int maximumSafePower;
  
  protected int tickRate;
  
  protected int updateTicker;
  
  protected int progress;
  
  protected int operationLength;
  
  @GuiSynced
  protected float guiProgress;
  
  protected AudioSource audioSource;
  
  protected static final int EventStart = 0;
  
  protected static final int EventInterrupt = 1;
  
  protected static final int EventFinish = 2;
  
  protected static final int EventStop = 3;
  
  public TileEntityKineticMachine(int minimumPowerRequired, int maximumSafePower) {
    this.tickRate = 20;
    this.progress = 0;
    this.operationLength = 200;
    this.minimumPowerRequired = minimumPowerRequired;
    this.maximumSafePower = maximumSafePower;
    this.updateTicker = IC2.random.nextInt(this.tickRate);
  }
  
  protected void onUnloaded() {
    super.onUnloaded();
    if ((getWorld()).isRemote && this.audioSource != null) {
      IC2.audioManager.removeSources(this);
      this.audioSource = null;
    } 
  }
  
  protected abstract boolean canOperate();
  
  protected abstract boolean hasValidInput();
  
  protected abstract boolean searchForValidInput();
  
  protected abstract int getAvailablePower();
  
  protected abstract boolean operateOnce();
  
  protected abstract void clearInput();
  
  public int getMinimumPowerRequired() {
    return this.minimumPowerRequired;
  }
  
  public int getMaximumSafePower() {
    return this.maximumSafePower;
  }
  
  public void destroy() {}
  
  public ContainerBase<? extends TileEntityKineticMachine> getGuiContainer(EntityPlayer player) {
    return (ContainerBase<? extends TileEntityKineticMachine>)DynamicContainer.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  @SideOnly(Side.CLIENT)
  public GuiScreen getGui(EntityPlayer player, boolean isAdmin) {
    return (GuiScreen)DynamicGui.create((IInventory)this, player, GuiParser.parse(this.teBlock));
  }
  
  public void onGuiClosed(EntityPlayer player) {}
  
  public double getGuiValue(String name) {
    return 0.0D;
  }
  
  public String getStartSoundFile() {
    return null;
  }
  
  public String getInterruptSoundFile() {
    return null;
  }
  
  public void onNetworkEvent(int event) {
    if (this.audioSource == null && getStartSoundFile() != null)
      this.audioSource = IC2.audioManager.createSource(this, getStartSoundFile()); 
    switch (event) {
      case 0:
        if (this.audioSource != null)
          this.audioSource.play(); 
        break;
      case 2:
        if (this.audioSource != null)
          this.audioSource.stop(); 
        break;
      case 1:
        if (this.audioSource != null) {
          this.audioSource.stop();
          if (getInterruptSoundFile() != null)
            IC2.audioManager.playOnce(this, PositionSpec.Center, getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume()); 
        } 
        break;
    } 
  }
}
