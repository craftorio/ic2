// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.comp;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.api.energy.EnergyNet;
import java.util.Iterator;
import java.io.IOException;
import java.io.DataInput;
import ic2.core.network.GrowingBuffer;
import net.minecraft.entity.player.EntityPlayerMP;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.tileentity.TileEntity;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.nbt.NBTTagCompound;
import java.util.ArrayList;
import ic2.api.energy.tile.IDischargingSlot;
import ic2.api.energy.tile.IChargingSlot;
import java.util.Collections;
import ic2.core.util.Util;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlot;
import java.util.List;
import net.minecraft.util.EnumFacing;
import java.util.Set;

public class Energy extends TileEntityComponent
{
    private static final boolean debugLoad;
    private double capacity;
    private double storage;
    private int sinkTier;
    private int sourceTier;
    private Set<EnumFacing> sinkDirections;
    private Set<EnumFacing> sourceDirections;
    private List<InvSlot> managedSlots;
    private boolean multiSource;
    private int sourcePackets;
    private EnergyNetDelegate delegate;
    private boolean loaded;
    private boolean receivingDisabled;
    private boolean sendingSidabled;
    private final boolean fullEnergy;
    
    public static Energy asBasicSink(final TileEntityBlock parent, final double capacity) {
        return asBasicSink(parent, capacity, 1);
    }
    
    public static Energy asBasicSink(final TileEntityBlock parent, final double capacity, final int tier) {
        return new Energy(parent, capacity, Util.allFacings, Collections.emptySet(), tier);
    }
    
    public static Energy asBasicSource(final TileEntityBlock parent, final double capacity) {
        return asBasicSource(parent, capacity, 1);
    }
    
    public static Energy asBasicSource(final TileEntityBlock parent, final double capacity, final int tier) {
        return new Energy(parent, capacity, Collections.emptySet(), Util.allFacings, tier);
    }
    
    public Energy(final TileEntityBlock parent, final double capacity) {
        this(parent, capacity, Collections.emptySet(), Collections.emptySet(), 1);
    }
    
    public Energy(final TileEntityBlock parent, final double capacity, final Set<EnumFacing> sinkDirections, final Set<EnumFacing> sourceDirections, final int tier) {
        this(parent, capacity, sinkDirections, sourceDirections, tier, tier, false);
    }
    
    public Energy(final TileEntityBlock parent, final double capacity, final Set<EnumFacing> sinkDirections, final Set<EnumFacing> sourceDirections, final int sinkTier, final int sourceTier, final boolean fullEnergy) {
        super(parent);
        this.multiSource = false;
        this.sourcePackets = 1;
        this.capacity = capacity;
        this.sinkTier = sinkTier;
        this.sourceTier = sourceTier;
        this.sinkDirections = sinkDirections;
        this.sourceDirections = sourceDirections;
        this.fullEnergy = fullEnergy;
    }
    
    public Energy addManagedSlot(final InvSlot slot) {
        if (slot instanceof IChargingSlot || slot instanceof IDischargingSlot) {
            if (this.managedSlots == null) {
                this.managedSlots = new ArrayList<InvSlot>(4);
            }
            this.managedSlots.add(slot);
            return this;
        }
        throw new IllegalArgumentException("No charge/discharge slot.");
    }
    
    public Energy setMultiSource(final boolean multiSource) {
        if (!(this.multiSource = multiSource)) {
            this.sourcePackets = 1;
        }
        return this;
    }
    
    @Override
    public void readFromNbt(final NBTTagCompound nbt) {
        this.storage = nbt.getDouble("storage");
    }
    
    @Override
    public NBTTagCompound writeToNbt() {
        final NBTTagCompound ret = new NBTTagCompound();
        ret.setDouble("storage", this.storage);
        return ret;
    }
    
    @Override
    public void onLoaded() {
        assert this.delegate == null;
        if (!this.parent.getWorld().isRemote) {
            if (!this.sinkDirections.isEmpty() || !this.sourceDirections.isEmpty()) {
                if (Energy.debugLoad) {
                    IC2.log.debug(LogCategory.Component, "Energy onLoaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
                }
                this.createDelegate();
                MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this.delegate));
            }
            else if (Energy.debugLoad) {
                IC2.log.debug(LogCategory.Component, "Skipping Energy onLoaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
            }
            this.loaded = true;
        }
    }
    
    private void createDelegate() {
        if (this.delegate != null) {
            throw new IllegalStateException();
        }
        assert !this.sourceDirections.isEmpty();
        if (this.sinkDirections.isEmpty()) {
            this.delegate = new EnergyNetDelegateSource();
        }
        else if (this.sourceDirections.isEmpty()) {
            this.delegate = new EnergyNetDelegateSink();
        }
        else {
            this.delegate = new EnergyNetDelegateDual();
        }
        this.delegate.setWorld(this.parent.getWorld());
        this.delegate.setPos(this.parent.getPos());
    }
    
    @Override
    public void onUnloaded() {
        if (this.delegate != null) {
            if (Energy.debugLoad) {
                IC2.log.debug(LogCategory.Component, "Energy onUnloaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
            }
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this.delegate));
            this.delegate = null;
        }
        else if (Energy.debugLoad) {
            IC2.log.debug(LogCategory.Component, "Skipping Energy onUnloaded for %s at %s.", this.parent, Util.formatPosition(this.parent));
        }
        this.loaded = false;
    }
    
    @Override
    public void onContainerUpdate(final EntityPlayerMP player) {
        final GrowingBuffer buffer = new GrowingBuffer(16);
        buffer.writeDouble(this.capacity);
        buffer.writeDouble(this.storage);
        buffer.flip();
        this.setNetworkUpdate(player, buffer);
    }
    
    @Override
    public void onNetworkUpdate(final DataInput is) throws IOException {
        this.capacity = is.readDouble();
        this.storage = is.readDouble();
    }
    
    @Override
    public boolean enableWorldTick() {
        return !this.parent.getWorld().isRemote && this.managedSlots != null;
    }
    
    @Override
    public void onWorldTick() {
        for (final InvSlot slot : this.managedSlots) {
            if (slot instanceof IChargingSlot) {
                if (this.storage <= 0.0) {
                    continue;
                }
                this.storage -= ((IChargingSlot)slot).charge(this.storage);
            }
            else {
                if (!(slot instanceof IDischargingSlot)) {
                    continue;
                }
                final double space = this.capacity - this.storage;
                if (space <= 0.0) {
                    continue;
                }
                this.storage += ((IDischargingSlot)slot).discharge(space, false);
            }
        }
    }
    
    public double getCapacity() {
        return this.capacity;
    }
    
    public void setCapacity(final double capacity) {
        this.capacity = capacity;
    }
    
    public double getEnergy() {
        return this.storage;
    }
    
    public double getFreeEnergy() {
        return Math.max(0.0, this.capacity - this.storage);
    }
    
    public double getFillRatio() {
        return this.storage / this.capacity;
    }
    
    public int getComparatorValue() {
        return Math.min((int)(this.storage * 15.0 / this.capacity), 15);
    }
    
    public double addEnergy(double amount) {
        amount = Math.min(this.capacity - this.storage, amount);
        this.storage += amount;
        return amount;
    }
    
    public void forceAddEnergy(final double amount) {
        this.storage += amount;
    }
    
    public boolean canUseEnergy(final double amount) {
        return this.storage >= amount;
    }
    
    public boolean useEnergy(final double amount) {
        if (this.storage >= amount) {
            this.storage -= amount;
            return true;
        }
        return false;
    }
    
    public double useEnergy(final double amount, final boolean simulate) {
        final double ret = Math.abs(Math.max(0.0, amount - this.storage) - amount);
        if (simulate) {
            return ret;
        }
        this.storage -= ret;
        return ret;
    }
    
    public int getSinkTier() {
        return this.sinkTier;
    }
    
    public void setSinkTier(final int tier) {
        this.sinkTier = tier;
    }
    
    public int getSourceTier() {
        return this.sourceTier;
    }
    
    public void setSourceTier(final int tier) {
        this.sourceTier = tier;
    }
    
    public void setEnabled(final boolean enabled) {
        final boolean b = !enabled;
        this.sendingSidabled = b;
        this.receivingDisabled = b;
    }
    
    public void setReceivingEnabled(final boolean enabled) {
        this.receivingDisabled = !enabled;
    }
    
    public void setSendingEnabled(final boolean enabled) {
        this.sendingSidabled = !enabled;
    }
    
    public boolean isMultiSource() {
        return this.multiSource;
    }
    
    public void setPacketOutput(final int number) {
        if (this.multiSource) {
            this.sourcePackets = number;
        }
    }
    
    public int getPacketOutput() {
        return this.sourcePackets;
    }
    
    public void setDirections(final Set<EnumFacing> sinkDirections, final Set<EnumFacing> sourceDirections) {
        if (sinkDirections.equals(this.sinkDirections) && sourceDirections.equals(this.sourceDirections)) {
            if (Energy.debugLoad) {
                IC2.log.debug(LogCategory.Component, "Energy setDirections unchanged for %s at %s, sink: %s, source: %s.", this.parent, Util.formatPosition(this.parent), sinkDirections, sourceDirections);
            }
            return;
        }
        if (this.delegate != null) {
            if (Energy.debugLoad) {
                IC2.log.debug(LogCategory.Component, "Energy setDirections unload for %s at %s.", this.parent, Util.formatPosition(this.parent));
            }
            assert !this.parent.getWorld().isRemote;
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this.delegate));
        }
        this.sinkDirections = sinkDirections;
        this.sourceDirections = sourceDirections;
        if (sinkDirections.isEmpty() && sourceDirections.isEmpty()) {
            this.delegate = null;
        }
        else if (this.delegate == null && this.loaded) {
            this.createDelegate();
        }
        if (this.delegate != null) {
            if (Energy.debugLoad) {
                IC2.log.debug(LogCategory.Component, "Energy setDirections load for %s at %s, sink: %s, source: %s.", this.parent, Util.formatPosition(this.parent), sinkDirections, sourceDirections);
            }
            assert !this.parent.getWorld().isRemote;
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this.delegate));
        }
        else if (Energy.debugLoad) {
            IC2.log.debug(LogCategory.Component, "Skipping Energy setDirections load for %s at %s, sink: %s, source: %s, loaded: %b.", this.parent, Util.formatPosition(this.parent), sinkDirections, sourceDirections, this.loaded);
        }
    }
    
    public Set<EnumFacing> getSourceDirs() {
        return Collections.unmodifiableSet((Set<? extends EnumFacing>)this.sourceDirections);
    }
    
    public Set<EnumFacing> getSinkDirs() {
        return Collections.unmodifiableSet((Set<? extends EnumFacing>)this.sinkDirections);
    }
    
    public IEnergyTile getDelegate() {
        return this.delegate;
    }
    
    private double getSourceEnergy() {
        if (this.fullEnergy) {
            return (this.storage >= EnergyNet.instance.getPowerFromTier(this.sourceTier)) ? this.storage : 0.0;
        }
        return this.storage;
    }
    
    private int getPacketCount() {
        if (this.fullEnergy) {
            return Math.min(this.sourcePackets, (int)Math.floor(this.storage / EnergyNet.instance.getPowerFromTier(this.sourceTier)));
        }
        return this.sourcePackets;
    }
    
    static {
        debugLoad = (System.getProperty("ic2.comp.energy.debugload") != null);
    }
    
    private abstract class EnergyNetDelegate extends TileEntity implements IEnergyTile
    {
    }
    
    private class EnergyNetDelegateSource extends EnergyNetDelegate implements IMultiEnergySource
    {
        public int getSourceTier() {
            return Energy.this.sourceTier;
        }
        
        public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing dir) {
            return Energy.this.sourceDirections.contains(dir);
        }
        
        public double getOfferedEnergy() {
            assert !Energy.this.sourceDirections.isEmpty();
            return Energy.this.sendingSidabled ? 0.0 : Energy.this.getSourceEnergy();
        }
        
        public void drawEnergy(final double amount) {
            assert amount <= Energy.this.storage;
            Energy.this.storage -= amount;
        }
        
        @Override
        public boolean sendMultipleEnergyPackets() {
            return Energy.this.multiSource;
        }
        
        @Override
        public int getMultipleEnergyPacketAmount() {
            return Energy.this.getPacketCount();
        }
    }
    
    private class EnergyNetDelegateSink extends EnergyNetDelegate implements IEnergySink
    {
        @Override
        public int getSinkTier() {
            return Energy.this.sinkTier;
        }
        
        public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing dir) {
            return Energy.this.sinkDirections.contains(dir);
        }
        
        @Override
        public double getDemandedEnergy() {
            assert !Energy.this.sinkDirections.isEmpty();
            return (!Energy.this.receivingDisabled && Energy.this.storage < Energy.this.capacity) ? (Energy.this.capacity - Energy.this.storage) : 0.0;
        }
        
        @Override
        public double injectEnergy(final EnumFacing directionFrom, final double amount, final double voltage) {
            Energy.this.storage += amount;
            return 0.0;
        }
    }
    
    private class EnergyNetDelegateDual extends EnergyNetDelegate implements IEnergySink, IMultiEnergySource
    {
        public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing dir) {
            return Energy.this.sinkDirections.contains(dir);
        }
        
        public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing dir) {
            return Energy.this.sourceDirections.contains(dir);
        }
        
        @Override
        public double getDemandedEnergy() {
            return (!Energy.this.receivingDisabled && !Energy.this.sinkDirections.isEmpty() && Energy.this.storage < Energy.this.capacity) ? (Energy.this.capacity - Energy.this.storage) : 0.0;
        }
        
        public double getOfferedEnergy() {
            return (!Energy.this.sendingSidabled && !Energy.this.sourceDirections.isEmpty()) ? Energy.this.getSourceEnergy() : 0.0;
        }
        
        @Override
        public int getSinkTier() {
            return Energy.this.sinkTier;
        }
        
        public int getSourceTier() {
            return Energy.this.sourceTier;
        }
        
        @Override
        public double injectEnergy(final EnumFacing directionFrom, final double amount, final double voltage) {
            Energy.this.storage += amount;
            return 0.0;
        }
        
        public void drawEnergy(final double amount) {
            assert amount <= Energy.this.storage;
            Energy.this.storage -= amount;
        }
        
        @Override
        public boolean sendMultipleEnergyPackets() {
            return Energy.this.multiSource;
        }
        
        @Override
        public int getMultipleEnergyPacketAmount() {
            return Energy.this.getPacketCount();
        }
    }
}
