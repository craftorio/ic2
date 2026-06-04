// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import ic2.core.audio.PositionSpec;
import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.recipe.MachineRecipeResult;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import ic2.core.network.NetworkManager;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.network.GuiSynced;
import ic2.core.audio.FutureSound;
import ic2.core.audio.AudioSource;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotProcessableSmelting;
import ic2.api.network.INetworkTileEntityEventListener;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

public class TileEntityInduction extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider, INetworkTileEntityEventListener
{
    private static final short maxHeat = 10000;
    public final InvSlotProcessableSmelting inputSlotA;
    public final InvSlotProcessableSmelting inputSlotB;
    public final InvSlotUpgrade upgradeSlot;
    public final InvSlotOutput outputSlotA;
    public final InvSlotOutput outputSlotB;
    protected final Redstone redstone;
    protected AudioSource audioSource;
    protected FutureSound startingSound;
    protected String finishingSound;
    @GuiSynced
    public short heat;
    @GuiSynced
    public short progress;
    
    public TileEntityInduction() {
        super(10000, 2);
        this.heat = 0;
        this.progress = 0;
        this.inputSlotA = new InvSlotProcessableSmelting(this, "inputA", 1);
        this.inputSlotB = new InvSlotProcessableSmelting(this, "inputB", 1);
        this.outputSlotA = new InvSlotOutput(this, "outputA", 1);
        this.outputSlotB = new InvSlotOutput(this, "outputB", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 2);
        this.redstone = this.addComponent(new Redstone(this));
        this.comparator.setUpdate(() -> this.heat * 15 / 10000);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.heat = nbt.getShort("heat");
        this.progress = nbt.getShort("progress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("heat", this.heat);
        nbt.setShort("progress", this.progress);
        return nbt;
    }
    
    protected void onUnloaded() {
        super.onUnloaded();
        if (IC2.platform.isRendering()) {
            if (this.startingSound != null) {
                if (!this.startingSound.isComplete()) {
                    this.startingSound.cancel();
                }
                this.startingSound = null;
            }
            if (this.finishingSound != null) {
                IC2.audioManager.removeSource(this.finishingSound);
                this.finishingSound = null;
            }
            if (this.audioSource != null) {
                IC2.audioManager.removeSources(this);
                this.audioSource = null;
            }
        }
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        boolean newActive = this.getActive();
        if (this.heat == 0) {
            newActive = false;
        }
        if (this.progress >= 4000) {
            this.operate();
            needsInvUpdate = true;
            this.progress = 0;
            newActive = false;
        }
        final boolean canOperate = this.canOperate();
        if ((canOperate || this.redstone.hasRedstoneInput()) && this.energy.useEnergy(1.0)) {
            if (this.heat < 10000) {
                ++this.heat;
            }
            newActive = true;
        }
        else {
            this.heat -= (short)Math.min(this.heat, 4);
        }
        if (!newActive || this.progress == 0) {
            if (canOperate) {
                if (this.energy.getEnergy() >= 15.0) {
                    newActive = true;
                    IC2.network.get(true).initiateTileEntityEvent(this, 0, true);
                }
            }
            else {
                if (needsInvUpdate) {
                    IC2.network.get(true).initiateTileEntityEvent(this, 3, true);
                }
                this.progress = 0;
            }
        }
        else if (!canOperate || this.energy.getEnergy() < 15.0) {
            if (!canOperate) {
                this.progress = 0;
            }
            newActive = false;
            IC2.network.get(true).initiateTileEntityEvent(this, 1, true);
        }
        if (newActive && canOperate) {
            this.progress += (short)(this.heat / 30);
            this.energy.useEnergy(15.0);
        }
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            this.markDirty();
        }
        if (newActive != this.getActive()) {
            this.setActive(newActive);
        }
    }
    
    public String getHeat() {
        return "" + this.heat * 100 / 10000 + "%";
    }
    
    public int gaugeProgressScaled(final int i) {
        return i * this.progress / 4000;
    }
    
    public void operate() {
        this.operate(this.inputSlotA, this.outputSlotA);
        this.operate(this.inputSlotB, this.outputSlotB);
    }
    
    public void operate(final InvSlotProcessableSmelting inputSlot, final InvSlotOutput outputSlot) {
        if (!this.canOperate(inputSlot, outputSlot)) {
            return;
        }
        final MachineRecipeResult<ItemStack, ItemStack, ItemStack> result = inputSlot.process();
        outputSlot.add(result.getOutput());
        inputSlot.consume(result);
    }
    
    public boolean canOperate() {
        return this.canOperate(this.inputSlotA, this.outputSlotA) || this.canOperate(this.inputSlotB, this.outputSlotB);
    }
    
    public boolean canOperate(final InvSlotProcessableSmelting inputSlot, final InvSlotOutput outputSlot) {
        if (inputSlot.isEmpty()) {
            return false;
        }
        final MachineRecipeResult<? extends ItemStack, ? extends ItemStack, ? extends ItemStack> result = inputSlot.process();
        return result != null && outputSlot.canAdd((ItemStack)result.getOutput());
    }
    
    @Override
    public ContainerBase<TileEntityInduction> getGuiContainer(final EntityPlayer player) {
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
    public double getEnergy() {
        return this.energy.getEnergy();
    }
    
    @Override
    public boolean useEnergy(final double amount) {
        return this.energy.useEnergy(amount);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.RedstoneSensitive, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
    }
    
    @Override
    public double getGuiValue(final String name) {
        if ("progress".equals(name)) {
            return this.gaugeProgressScaled(1000) / 1000.0;
        }
        throw new IllegalArgumentException();
    }
    
    public String getStartingSoundFile() {
        return "Machines/Induction Furnace/InductionStart.ogg";
    }
    
    public String getStartSoundFile() {
        return "Machines/Induction Furnace/InductionLoop.ogg";
    }
    
    public String getInterruptSoundFile() {
        return "Machines/Induction Furnace/InductionStop.ogg";
    }
    
    @Override
    public void onNetworkEvent(final int event) {
        if (this.audioSource == null && this.getStartSoundFile() != null) {
            this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, this.getStartSoundFile(), true, false, IC2.audioManager.getDefaultVolume());
        }
        switch (event) {
            case 0: {
                if (this.startingSound == null) {
                    if (this.finishingSound != null) {
                        IC2.audioManager.removeSource(this.finishingSound);
                        this.finishingSound = null;
                    }
                    final String source = IC2.audioManager.playOnce(this, PositionSpec.Center, this.getStartingSoundFile(), false, IC2.audioManager.getDefaultVolume());
                    if (this.audioSource != null) {
                        IC2.audioManager.chainSource(source, this.startingSound = new FutureSound(this.audioSource::play));
                    }
                    break;
                }
                break;
            }
            case 1:
            case 3: {
                if (this.audioSource != null) {
                    this.audioSource.stop();
                    if (this.startingSound != null) {
                        if (!this.startingSound.isComplete()) {
                            this.startingSound.cancel();
                        }
                        this.startingSound = null;
                    }
                    this.finishingSound = IC2.audioManager.playOnce(this, PositionSpec.Center, this.getInterruptSoundFile(), false, IC2.audioManager.getDefaultVolume());
                    break;
                }
                break;
            }
        }
    }
}
