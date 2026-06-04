// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import ic2.core.audio.PositionSpec;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.gui.dynamic.DynamicGui;
import net.minecraft.client.gui.GuiScreen;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.ContainerBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.core.util.PumpUtil;
import ic2.core.util.LiquidUtil;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.tileentity.TileEntity;
import java.util.Iterator;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import ic2.core.util.Util;
import net.minecraftforge.fluids.IFluidTank;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.audio.AudioSource;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

public class TileEntityPump extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, IGuiValueProvider
{
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
        this.containerSlot = new InvSlotConsumableLiquid(this, "input", InvSlot.Access.I, 1, InvSlot.InvSide.TOP, InvSlotConsumableLiquid.OpType.Fill);
        this.outputSlot = new InvSlotOutput(this, "output", 1, InvSlot.InvSide.SIDE);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        final int n = 1;
        this.energyConsume = n;
        this.defaultEnergyConsume = n;
        final int n2 = 20;
        this.operationLength = n2;
        this.defaultOperationLength = n2;
        this.defaultTier = 1;
        this.defaultEnergyStorage = 1 * this.operationLength;
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTankExtract("fluid", 8000);
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (!this.getWorld().isRemote) {
            this.setUpgradestat();
        }
    }
    
    protected void onUnloaded() {
        if (IC2.platform.isRendering() && this.audioSource != null) {
            IC2.audioManager.removeSources(this);
            this.audioSource = null;
        }
        this.miner = null;
        super.onUnloaded();
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.progress = nbt.getShort("progress");
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("progress", this.progress);
        return nbt;
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.canoperate() && this.energy.getEnergy() >= this.energyConsume * this.operationLength) {
            if (this.progress < this.operationLength) {
                ++this.progress;
                this.energy.useEnergy(this.energyConsume);
            }
            else {
                this.progress = 0;
                this.operate(false);
            }
            this.setActive(true);
        }
        else {
            this.setActive(false);
        }
        needsInvUpdate |= this.containerSlot.processFromTank((IFluidTank)this.fluidTank, this.outputSlot);
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        this.guiProgress = this.progress / (float)this.operationLength;
        if (needsInvUpdate) {
            super.markDirty();
        }
    }
    
    public boolean canoperate() {
        return this.operate(true);
    }
    
    public boolean operate(final boolean sim) {
        if (this.miner == null || this.miner.isInvalid()) {
            this.miner = null;
            final World world = this.getWorld();
            for (final EnumFacing dir : Util.downSideFacings) {
                final TileEntity te = world.getTileEntity(this.pos.offset(dir));
                if (te instanceof TileEntityMiner) {
                    this.miner = (TileEntityMiner)te;
                    break;
                }
            }
        }
        FluidStack liquid = null;
        if (this.miner != null) {
            if (this.miner.canProvideLiquid) {
                liquid = this.pump(this.miner.liquidPos, sim, this.miner);
            }
        }
        else {
            final EnumFacing dir2 = this.getFacing();
            liquid = this.pump(this.pos.offset(dir2), sim, this.miner);
        }
        if (liquid != null && this.fluidTank.fillInternal(liquid, false) > 0) {
            if (!sim) {
                this.fluidTank.fillInternal(liquid, true);
            }
            return true;
        }
        return false;
    }
    
    public FluidStack pump(final BlockPos startPos, final boolean sim, final TileEntityMiner miner) {
        final World world = this.getWorld();
        int freeSpace = this.fluidTank.getCapacity() - this.fluidTank.getFluidAmount();
        if (miner == null && freeSpace > 0) {
            final TileEntity te = world.getTileEntity(startPos);
            final EnumFacing side = this.getFacing().getOpposite();
            if (te != null && LiquidUtil.isFluidTile(te, side)) {
                if (freeSpace > 1000) {
                    freeSpace = 1000;
                }
                return LiquidUtil.drainTile(te, side, freeSpace, sim);
            }
        }
        if (freeSpace >= 1000) {
            BlockPos cPos;
            if (miner != null && miner.canProvideLiquid) {
                assert miner.liquidPos != null;
                cPos = miner.liquidPos;
            }
            else {
                cPos = PumpUtil.searchFluidSource(world, startPos);
            }
            if (cPos != null) {
                return LiquidUtil.drainBlock(world, cPos, sim);
            }
        }
        return null;
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setUpgradestat();
        }
    }
    
    public void setUpgradestat() {
        final double previousProgress = this.progress / (double)this.operationLength;
        this.operationsPerTick = this.upgradeSlot.getOperationsPerTick(this.defaultOperationLength);
        this.operationLength = this.upgradeSlot.getOperationLength(this.defaultOperationLength);
        this.energyConsume = this.upgradeSlot.getEnergyDemand(this.defaultEnergyConsume);
        this.energy.setSinkTier(this.upgradeSlot.getTier(this.defaultTier));
        this.dischargeSlot.setTier(this.energy.getSinkTier());
        this.energy.setCapacity(this.upgradeSlot.getEnergyStorage(this.defaultEnergyStorage, this.defaultOperationLength, this.defaultEnergyConsume));
        this.progress = (short)Math.floor(previousProgress * this.operationLength + 0.1);
    }
    
    @Override
    public double getGuiValue(final String name) {
        if (name.equals("progress")) {
            return this.guiProgress;
        }
        throw new IllegalArgumentException(this.getClass().getSimpleName() + " Cannot get value for " + name);
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
    public ContainerBase<TileEntityPump> getGuiContainer(final EntityPlayer player) {
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
    
    public void onNetworkUpdate(final String field) {
        if (field.equals("active")) {
            if (this.audioSource == null) {
                this.audioSource = IC2.audioManager.createSource(this, PositionSpec.Center, "Machines/PumpOp.ogg", true, false, IC2.audioManager.getDefaultVolume());
            }
            if (this.getActive()) {
                if (this.audioSource != null) {
                    this.audioSource.play();
                }
            }
            else if (this.audioSource != null) {
                this.audioSource.stop();
            }
        }
        super.onNetworkUpdate(field);
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidProducing);
    }
}
