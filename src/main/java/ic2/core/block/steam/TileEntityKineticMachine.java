// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.steam;

import ic2.core.audio.PositionSpec;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.IC2;
import ic2.core.audio.AudioSource;
import ic2.core.network.GuiSynced;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;

public abstract class TileEntityKineticMachine extends TileEntityInventory implements IKineticMachine, IHasGui, IGuiValueProvider, INetworkTileEntityEventListener
{
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
    
    public TileEntityKineticMachine(final int minimumPowerRequired, final int maximumSafePower) {
        this.tickRate = 20;
        this.progress = 0;
        this.operationLength = 200;
        this.minimumPowerRequired = minimumPowerRequired;
        this.maximumSafePower = maximumSafePower;
        this.updateTicker = IC2.random.nextInt(this.tickRate);
    }
    
    @Override
    protected void onUnloaded() {
        super.onUnloaded();
        if (this.getWorld().isRemote && this.audioSource != null) {
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
    
    @Override
    public int getMinimumPowerRequired() {
        return this.minimumPowerRequired;
    }
    
    @Override
    public int getMaximumSafePower() {
        return this.maximumSafePower;
    }
    
    @Override
    public void destroy() {
    }
    
    @Override
    public ContainerBase<? extends TileEntityKineticMachine> getGuiContainer(final EntityPlayer player) {
        return DynamicContainer.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)DynamicGui.create(this, player, GuiParser.parse(this.teBlock));
    }
    
    @Override
    public void onGuiClosed(final EntityPlayer player) {
    }
    
    @Override
    public double getGuiValue(final String name) {
        return 0.0;
    }
    
    public String getStartSoundFile() {
        return null;
    }
    
    public String getInterruptSoundFile() {
        return null;
    }
    
    @Override
    public void onNetworkEvent(final int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IC2.audioManager.createSource(this, this.getStartSoundFile());
        }
        switch (event) {
            case 0: {
                if (this.audioSource != null) {
                    this.audioSource.play();
                    break;
                }
                break;
            }
            case 2: {
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    break;
                }
                break;
            }
            case 1: {
                if (this.audioSource == null) {
                    break;
                }
                this.audioSource.stop();
                if (this.getInterruptSoundFile() != null) {
                    IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
                    break;
                }
                break;
            }
        }
    }
}
