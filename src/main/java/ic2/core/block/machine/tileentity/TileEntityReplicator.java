// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.machine.tileentity;

import java.util.EnumSet;
import ic2.api.upgrade.UpgradableProperty;
import java.util.Set;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.IFluidTank;
import ic2.core.IC2;
import ic2.core.ContainerBase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import ic2.core.block.machine.gui.GuiReplicator;
import ic2.core.block.machine.container.ContainerReplicator;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.util.EnumFacing;
import java.util.List;
import ic2.api.recipe.IPatternStorage;
import ic2.core.util.StackUtil;
import ic2.core.uu.UuIndex;
import net.minecraftforge.fluids.FluidStack;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.invslot.InvSlotConsumableLiquidByList;
import ic2.core.ref.FluidName;
import net.minecraftforge.fluids.Fluid;
import ic2.core.block.comp.Fluids;
import ic2.core.network.GuiSynced;
import net.minecraftforge.fluids.FluidTank;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import net.minecraft.item.ItemStack;
import ic2.core.profile.NotClassic;
import ic2.api.network.INetworkClientTileEntityEventListener;
import ic2.api.upgrade.IUpgradableBlock;
import ic2.core.IHasGui;

@NotClassic
public class TileEntityReplicator extends TileEntityElectricMachine implements IHasGui, IUpgradableBlock, INetworkClientTileEntityEventListener
{
    private static final double uuPerTickBase = 1.0E-4;
    private static final double euPerTickBase = 512.0;
    private static final int defaultTier = 4;
    private static final int defaultEnergyStorage = 2000000;
    private double uuPerTick;
    private double euPerTick;
    private double extraUuStored;
    public double uuProcessed;
    public ItemStack pattern;
    private Mode mode;
    public int index;
    public int maxIndex;
    public double patternUu;
    public double patternEu;
    public final InvSlotConsumableLiquid fluidSlot;
    public final InvSlotOutput cellSlot;
    public final InvSlotOutput outputSlot;
    public final InvSlotUpgrade upgradeSlot;
    @GuiSynced
    public final FluidTank fluidTank;
    protected final Fluids fluids;
    
    public TileEntityReplicator() {
        super(2000000, 4);
        this.uuPerTick = 1.0E-4;
        this.euPerTick = 512.0;
        this.extraUuStored = 0.0;
        this.uuProcessed = 0.0;
        this.mode = Mode.STOPPED;
        this.fluidSlot = new InvSlotConsumableLiquidByList(this, "fluid", 1, new Fluid[] { FluidName.uu_matter.getInstance() });
        this.cellSlot = new InvSlotOutput(this, "cell", 1);
        this.outputSlot = new InvSlotOutput(this, "output", 1);
        this.upgradeSlot = new InvSlotUpgrade((T)this, "upgrade", 4);
        this.fluids = this.addComponent(new Fluids(this));
        this.fluidTank = this.fluids.addTank("fluidTank", 16000, Fluids.fluidPredicate(FluidName.uu_matter.getInstance()));
    }
    
    protected void updateEntityServer() {
        super.updateEntityServer();
        boolean needsInvUpdate = false;
        if (this.fluidTank.getFluidAmount() < this.fluidTank.getCapacity()) {
            needsInvUpdate = this.gainFluid();
        }
        boolean newActive = false;
        if (this.mode != Mode.STOPPED && this.energy.getEnergy() >= this.euPerTick && this.pattern != null && this.outputSlot.canAdd(this.pattern)) {
            double uuRemaining = this.patternUu - this.uuProcessed;
            boolean finish;
            if (uuRemaining <= this.uuPerTick) {
                finish = true;
            }
            else {
                uuRemaining = this.uuPerTick;
                finish = false;
            }
            if (this.consumeUu(uuRemaining)) {
                newActive = true;
                this.energy.useEnergy(this.euPerTick);
                this.uuProcessed += uuRemaining;
                if (finish) {
                    this.uuProcessed = 0.0;
                    if (this.mode == Mode.SINGLE) {
                        this.mode = Mode.STOPPED;
                    }
                    else {
                        this.refreshInfo();
                    }
                    if (this.pattern != null) {
                        this.outputSlot.add(this.pattern);
                        needsInvUpdate = true;
                    }
                }
            }
        }
        this.setActive(newActive);
        needsInvUpdate |= this.upgradeSlot.tickNoMark();
        if (needsInvUpdate) {
            this.markDirty();
        }
    }
    
    private boolean consumeUu(double amount) {
        if (amount <= this.extraUuStored) {
            this.extraUuStored -= amount;
            return true;
        }
        amount -= this.extraUuStored;
        final int toDrain = (int)Math.ceil(amount * 1000.0);
        final FluidStack drained = this.fluidTank.drainInternal(toDrain, false);
        if (drained != null && drained.getFluid() == FluidName.uu_matter.getInstance() && drained.amount == toDrain) {
            this.fluidTank.drainInternal(toDrain, true);
            amount -= drained.amount / 1000.0;
            if (amount < 0.0) {
                this.extraUuStored = -amount;
            }
            else {
                this.extraUuStored = 0.0;
            }
            return true;
        }
        return false;
    }
    
    public void refreshInfo() {
        final IPatternStorage storage = this.getPatternStorage();
        final ItemStack oldPattern = this.pattern;
        if (storage == null) {
            this.pattern = null;
        }
        else {
            final List<ItemStack> patterns = storage.getPatterns();
            if (this.index < 0 || this.index >= patterns.size()) {
                this.index = 0;
            }
            this.maxIndex = patterns.size();
            if (patterns.isEmpty()) {
                this.pattern = null;
            }
            else {
                this.pattern = patterns.get(this.index);
                this.patternUu = UuIndex.instance.getInBuckets(this.pattern);
                if (!StackUtil.checkItemEqualityStrict(this.pattern, oldPattern)) {
                    this.uuProcessed = 0.0;
                    this.mode = Mode.STOPPED;
                }
            }
        }
        if (this.pattern == null) {
            this.uuProcessed = 0.0;
            this.mode = Mode.STOPPED;
        }
    }
    
    public IPatternStorage getPatternStorage() {
        final World world = this.getWorld();
        for (final EnumFacing dir : EnumFacing.VALUES) {
            final TileEntity target = world.getTileEntity(this.pos.offset(dir));
            if (target instanceof IPatternStorage) {
                return (IPatternStorage)target;
            }
        }
        return null;
    }
    
    public void setOverclockRates() {
        this.upgradeSlot.onChanged();
        this.uuPerTick = 1.0E-4 / this.upgradeSlot.processTimeMultiplier;
        this.euPerTick = (512.0 + this.upgradeSlot.extraEnergyDemand) * this.upgradeSlot.energyDemandMultiplier;
        this.energy.setSinkTier(applyModifier(4, this.upgradeSlot.extraTier, 1.0));
        this.energy.setCapacity(applyModifier(2000000, this.upgradeSlot.extraEnergyStorage, this.upgradeSlot.energyStorageMultiplier));
    }
    
    private static int applyModifier(final int base, final int extra, final double multiplier) {
        final double ret = (double)Math.round((base + (double)extra) * multiplier);
        return (ret > 2.147483647E9) ? Integer.MAX_VALUE : ((int)ret);
    }
    
    @SideOnly(Side.CLIENT)
    @Override
    public GuiScreen getGui(final EntityPlayer player, final boolean isAdmin) {
        return (GuiScreen)new GuiReplicator(new ContainerReplicator(player, this));
    }
    
    @Override
    public ContainerBase<TileEntityReplicator> getGuiContainer(final EntityPlayer player) {
        return new ContainerReplicator(player, this);
    }
    
    protected void onLoaded() {
        super.onLoaded();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
            this.refreshInfo();
        }
    }
    
    @Override
    public void markDirty() {
        super.markDirty();
        if (IC2.platform.isSimulating()) {
            this.setOverclockRates();
        }
    }
    
    public boolean gainFluid() {
        return this.fluidSlot.processIntoTank((IFluidTank)this.fluidTank, this.cellSlot);
    }
    
    @Override
    public void readFromNBT(final NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.extraUuStored = nbt.getDouble("extraUuStored");
        this.uuProcessed = nbt.getDouble("uuProcessed");
        this.index = nbt.getInteger("index");
        final int modeIdx = nbt.getInteger("mode");
        this.mode = ((modeIdx < Mode.values().length) ? Mode.values()[modeIdx] : Mode.STOPPED);
        final NBTTagCompound contentTag = nbt.getCompoundTag("pattern");
        this.pattern = new ItemStack(contentTag);
    }
    
    @Override
    public NBTTagCompound writeToNBT(final NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setDouble("extraUuStored", this.extraUuStored);
        nbt.setDouble("uuProcessed", this.uuProcessed);
        nbt.setInteger("index", this.index);
        nbt.setInteger("mode", this.mode.ordinal());
        if (this.pattern != null) {
            final NBTTagCompound contentTag = new NBTTagCompound();
            this.pattern.writeToNBT(contentTag);
            nbt.setTag("pattern", (NBTBase)contentTag);
        }
        return nbt;
    }
    
    @Override
    public void onNetworkEvent(final EntityPlayer player, final int event) {
        switch (event) {
            case 0:
            case 1: {
                if (this.mode == Mode.STOPPED) {
                    final IPatternStorage storage = this.getPatternStorage();
                    if (storage != null) {
                        final List<ItemStack> patterns = storage.getPatterns();
                        if (!patterns.isEmpty()) {
                            if (event == 0) {
                                if (this.index <= 0) {
                                    this.index = patterns.size() - 1;
                                }
                                else {
                                    --this.index;
                                }
                            }
                            else if (this.index >= patterns.size() - 1) {
                                this.index = 0;
                            }
                            else {
                                ++this.index;
                            }
                            this.refreshInfo();
                        }
                    }
                    break;
                }
                break;
            }
            case 3: {
                if (this.mode != Mode.STOPPED) {
                    this.uuProcessed = 0.0;
                    this.mode = Mode.STOPPED;
                    break;
                }
                break;
            }
            case 4: {
                if (this.pattern == null) {
                    break;
                }
                this.mode = Mode.SINGLE;
                if (player != null) {
                    IC2.achievements.issueAchievement(player, "replicateObject");
                    break;
                }
                break;
            }
            case 5: {
                if (this.pattern == null) {
                    break;
                }
                this.mode = Mode.CONTINUOUS;
                if (player != null) {
                    IC2.achievements.issueAchievement(player, "replicateObject");
                    break;
                }
                break;
            }
        }
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
    
    public Mode getMode() {
        return this.mode;
    }
    
    @Override
    public Set<UpgradableProperty> getUpgradableProperties() {
        return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing, UpgradableProperty.FluidConsuming);
    }
    
    public enum Mode
    {
        STOPPED, 
        SINGLE, 
        CONTINUOUS;
    }
}
