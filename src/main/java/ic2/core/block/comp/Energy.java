package ic2.core.block.comp;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.event.EnergyTileLoadEvent;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import ic2.api.energy.tile.IChargingSlot;
import ic2.api.energy.tile.IDischargingSlot;
import ic2.api.energy.tile.IEnergyAcceptor;
import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.energy.tile.IMultiEnergySource;
import ic2.core.IC2;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.invslot.InvSlot;
import ic2.core.network.GrowingBuffer;
import ic2.core.util.LogCategory;
import ic2.core.util.Util;
import java.io.DataInput;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.Event;

public class Energy extends TileEntityComponent {
  public static Energy asBasicSink(TileEntityBlock parent, double capacity) {
    return asBasicSink(parent, capacity, 1);
  }
  
  public static Energy asBasicSink(TileEntityBlock parent, double capacity, int tier) {
    return new Energy(parent, capacity, Util.allFacings, Collections.emptySet(), tier);
  }
  
  public static Energy asBasicSource(TileEntityBlock parent, double capacity) {
    return asBasicSource(parent, capacity, 1);
  }
  
  public static Energy asBasicSource(TileEntityBlock parent, double capacity, int tier) {
    return new Energy(parent, capacity, Collections.emptySet(), Util.allFacings, tier);
  }
  
  public Energy(TileEntityBlock parent, double capacity) {
    this(parent, capacity, Collections.emptySet(), Collections.emptySet(), 1);
  }
  
  public Energy(TileEntityBlock parent, double capacity, Set<EnumFacing> sinkDirections, Set<EnumFacing> sourceDirections, int tier) {
    this(parent, capacity, sinkDirections, sourceDirections, tier, tier, false);
  }
  
  public Energy(TileEntityBlock parent, double capacity, Set<EnumFacing> sinkDirections, Set<EnumFacing> sourceDirections, int sinkTier, int sourceTier, boolean fullEnergy) {
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
  
  public Energy addManagedSlot(InvSlot slot) {
    if (slot instanceof IChargingSlot || slot instanceof IDischargingSlot) {
      if (this.managedSlots == null)
        this.managedSlots = new ArrayList<>(4); 
      this.managedSlots.add(slot);
    } else {
      throw new IllegalArgumentException("No charge/discharge slot.");
    } 
    return this;
  }
  
  public Energy setMultiSource(boolean multiSource) {
    this.multiSource = multiSource;
    if (!multiSource)
      this.sourcePackets = 1; 
    return this;
  }
  
  public void readFromNbt(NBTTagCompound nbt) {
    this.storage = nbt.getDouble("storage");
  }
  
  public NBTTagCompound writeToNbt() {
    NBTTagCompound ret = new NBTTagCompound();
    ret.setDouble("storage", this.storage);
    return ret;
  }
  
  public void onLoaded() {
    assert this.delegate == null;
    if (!(this.parent.getWorld()).isRemote) {
      if (!this.sinkDirections.isEmpty() || !this.sourceDirections.isEmpty()) {
        if (debugLoad)
          IC2.log.debug(LogCategory.Component, "Energy onLoaded for %s at %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent) }); 
        createDelegate();
        MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this.delegate));
      } else if (debugLoad) {
        IC2.log.debug(LogCategory.Component, "Skipping Energy onLoaded for %s at %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent) });
      } 
      this.loaded = true;
    } 
  }
  
  private void createDelegate() {
    if (this.delegate != null)
      throw new IllegalStateException(); 
    assert !this.sinkDirections.isEmpty() || !this.sourceDirections.isEmpty();
    if (this.sinkDirections.isEmpty()) {
      this.delegate = new EnergyNetDelegateSource();
    } else if (this.sourceDirections.isEmpty()) {
      this.delegate = new EnergyNetDelegateSink();
    } else {
      this.delegate = new EnergyNetDelegateDual();
    } 
    this.delegate.func_145834_a(this.parent.getWorld());
    this.delegate.func_174878_a(this.parent.getPos());
  }
  
  public void onUnloaded() {
    if (this.delegate != null) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Component, "Energy onUnloaded for %s at %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent) }); 
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this.delegate));
      this.delegate = null;
    } else if (debugLoad) {
      IC2.log.debug(LogCategory.Component, "Skipping Energy onUnloaded for %s at %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent) });
    } 
    this.loaded = false;
  }
  
  public void onContainerUpdate(EntityPlayerMP player) {
    GrowingBuffer buffer = new GrowingBuffer(16);
    buffer.writeDouble(this.capacity);
    buffer.writeDouble(this.storage);
    buffer.flip();
    setNetworkUpdate(player, buffer);
  }
  
  public void onNetworkUpdate(DataInput is) throws IOException {
    this.capacity = is.readDouble();
    this.storage = is.readDouble();
  }
  
  public boolean enableWorldTick() {
    return (!(this.parent.getWorld()).isRemote && this.managedSlots != null);
  }
  
  public void onWorldTick() {
    for (InvSlot slot : this.managedSlots) {
      if (slot instanceof IChargingSlot) {
        if (this.storage > 0.0D)
          this.storage -= ((IChargingSlot)slot).charge(this.storage); 
        continue;
      } 
      if (slot instanceof IDischargingSlot) {
        double space = this.capacity - this.storage;
        if (space > 0.0D)
          this.storage += ((IDischargingSlot)slot).discharge(space, false); 
      } 
    } 
  }
  
  public double getCapacity() {
    return this.capacity;
  }
  
  public void setCapacity(double capacity) {
    this.capacity = capacity;
  }
  
  public double getEnergy() {
    return this.storage;
  }
  
  public double getFreeEnergy() {
    return Math.max(0.0D, this.capacity - this.storage);
  }
  
  public double getFillRatio() {
    return this.storage / this.capacity;
  }
  
  public int getComparatorValue() {
    return Math.min((int)(this.storage * 15.0D / this.capacity), 15);
  }
  
  public double addEnergy(double amount) {
    amount = Math.min(this.capacity - this.storage, amount);
    this.storage += amount;
    return amount;
  }
  
  public void forceAddEnergy(double amount) {
    this.storage += amount;
  }
  
  public boolean canUseEnergy(double amount) {
    return (this.storage >= amount);
  }
  
  public boolean useEnergy(double amount) {
    if (this.storage >= amount) {
      this.storage -= amount;
      return true;
    } 
    return false;
  }
  
  public double useEnergy(double amount, boolean simulate) {
    double ret = Math.abs(Math.max(0.0D, amount - this.storage) - amount);
    if (simulate)
      return ret; 
    this.storage -= ret;
    return ret;
  }
  
  public int getSinkTier() {
    return this.sinkTier;
  }
  
  public void setSinkTier(int tier) {
    this.sinkTier = tier;
  }
  
  public int getSourceTier() {
    return this.sourceTier;
  }
  
  public void setSourceTier(int tier) {
    this.sourceTier = tier;
  }
  
  public void setEnabled(boolean enabled) {
    this.receivingDisabled = this.sendingSidabled = !enabled;
  }
  
  public void setReceivingEnabled(boolean enabled) {
    this.receivingDisabled = !enabled;
  }
  
  public void setSendingEnabled(boolean enabled) {
    this.sendingSidabled = !enabled;
  }
  
  public boolean isMultiSource() {
    return this.multiSource;
  }
  
  public void setPacketOutput(int number) {
    if (this.multiSource)
      this.sourcePackets = number; 
  }
  
  public int getPacketOutput() {
    return this.sourcePackets;
  }
  
  public void setDirections(Set<EnumFacing> sinkDirections, Set<EnumFacing> sourceDirections) {
    if (sinkDirections.equals(this.sinkDirections) && sourceDirections.equals(this.sourceDirections)) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Component, "Energy setDirections unchanged for %s at %s, sink: %s, source: %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent), sinkDirections, sourceDirections }); 
      return;
    } 
    if (this.delegate != null) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Component, "Energy setDirections unload for %s at %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent) }); 
      assert !(this.parent.getWorld()).isRemote;
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this.delegate));
    } 
    this.sinkDirections = sinkDirections;
    this.sourceDirections = sourceDirections;
    if (sinkDirections.isEmpty() && sourceDirections.isEmpty()) {
      this.delegate = null;
    } else if (this.delegate == null && this.loaded) {
      createDelegate();
    } 
    if (this.delegate != null) {
      if (debugLoad)
        IC2.log.debug(LogCategory.Component, "Energy setDirections load for %s at %s, sink: %s, source: %s.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent), sinkDirections, sourceDirections }); 
      assert !(this.parent.getWorld()).isRemote;
      MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this.delegate));
    } else if (debugLoad) {
      IC2.log.debug(LogCategory.Component, "Skipping Energy setDirections load for %s at %s, sink: %s, source: %s, loaded: %b.", new Object[] { this.parent, Util.formatPosition((TileEntity)this.parent), sinkDirections, sourceDirections, Boolean.valueOf(this.loaded) });
    } 
  }
  
  public Set<EnumFacing> getSourceDirs() {
    return Collections.unmodifiableSet(this.sourceDirections);
  }
  
  public Set<EnumFacing> getSinkDirs() {
    return Collections.unmodifiableSet(this.sinkDirections);
  }
  
  public IEnergyTile getDelegate() {
    return this.delegate;
  }
  
  private double getSourceEnergy() {
    if (this.fullEnergy)
      return (this.storage >= EnergyNet.instance.getPowerFromTier(this.sourceTier)) ? this.storage : 0.0D; 
    return this.storage;
  }
  
  private int getPacketCount() {
    if (this.fullEnergy)
      return Math.min(this.sourcePackets, (int)Math.floor(this.storage / EnergyNet.instance.getPowerFromTier(this.sourceTier))); 
    return this.sourcePackets;
  }
  
  private abstract class EnergyNetDelegate extends TileEntity implements IEnergyTile {
    private EnergyNetDelegate() {}
  }
  
  private class EnergyNetDelegateSource extends EnergyNetDelegate implements IMultiEnergySource {
    private EnergyNetDelegateSource() {}
    
    public int getSourceTier() {
      return Energy.this.sourceTier;
    }
    
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing dir) {
      return Energy.this.sourceDirections.contains(dir);
    }
    
    public double getOfferedEnergy() {
      assert !Energy.this.sourceDirections.isEmpty();
      return !Energy.this.sendingSidabled ? Energy.this.getSourceEnergy() : 0.0D;
    }
    
    public void drawEnergy(double amount) {
      assert amount <= Energy.this.storage;
      Energy.this.storage = Energy.this.storage - amount;
    }
    
    public boolean sendMultipleEnergyPackets() {
      return Energy.this.multiSource;
    }
    
    public int getMultipleEnergyPacketAmount() {
      return Energy.this.getPacketCount();
    }
  }
  
  private class EnergyNetDelegateSink extends EnergyNetDelegate implements IEnergySink {
    private EnergyNetDelegateSink() {}
    
    public int getSinkTier() {
      return Energy.this.sinkTier;
    }
    
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing dir) {
      return Energy.this.sinkDirections.contains(dir);
    }
    
    public double getDemandedEnergy() {
      assert !Energy.this.sinkDirections.isEmpty();
      return (!Energy.this.receivingDisabled && Energy.this.storage < Energy.this.capacity) ? (Energy.this.capacity - Energy.this.storage) : 0.0D;
    }
    
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
      Energy.this.storage = Energy.this.storage + amount;
      return 0.0D;
    }
  }
  
  private class EnergyNetDelegateDual extends EnergyNetDelegate implements IEnergySink, IMultiEnergySource {
    private EnergyNetDelegateDual() {}
    
    public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing dir) {
      return Energy.this.sinkDirections.contains(dir);
    }
    
    public boolean emitsEnergyTo(IEnergyAcceptor receiver, EnumFacing dir) {
      return Energy.this.sourceDirections.contains(dir);
    }
    
    public double getDemandedEnergy() {
      return (!Energy.this.receivingDisabled && !Energy.this.sinkDirections.isEmpty() && Energy.this.storage < Energy.this.capacity) ? (Energy.this.capacity - Energy.this.storage) : 0.0D;
    }
    
    public double getOfferedEnergy() {
      return (!Energy.this.sendingSidabled && !Energy.this.sourceDirections.isEmpty()) ? Energy.this.getSourceEnergy() : 0.0D;
    }
    
    public int getSinkTier() {
      return Energy.this.sinkTier;
    }
    
    public int getSourceTier() {
      return Energy.this.sourceTier;
    }
    
    public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
      Energy.this.storage = Energy.this.storage + amount;
      return 0.0D;
    }
    
    public void drawEnergy(double amount) {
      assert amount <= Energy.this.storage;
      Energy.this.storage = Energy.this.storage - amount;
    }
    
    public boolean sendMultipleEnergyPackets() {
      return Energy.this.multiSource;
    }
    
    public int getMultipleEnergyPacketAmount() {
      return Energy.this.getPacketCount();
    }
  }
  
  private static final boolean debugLoad = (System.getProperty("ic2.comp.energy.debugload") != null);
  
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
}
