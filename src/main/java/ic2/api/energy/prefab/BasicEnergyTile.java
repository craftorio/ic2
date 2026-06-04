// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.prefab;

import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.energy.event.EnergyTileUnloadEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import ic2.api.energy.event.EnergyTileLoadEvent;
import net.minecraftforge.common.MinecraftForge;
import ic2.api.info.Info;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.info.ILocatable;

abstract class BasicEnergyTile implements ILocatable, IEnergyTile
{
    private final Object locationProvider;
    protected World world;
    protected BlockPos pos;
    protected double capacity;
    protected double energyStored;
    protected boolean addedToEnet;
    
    protected BasicEnergyTile(final TileEntity parent, final double capacity) {
        this((Object)parent, capacity);
    }
    
    protected BasicEnergyTile(final ILocatable parent, final double capacity) {
        this((Object)parent, capacity);
    }
    
    private BasicEnergyTile(final Object locationProvider, final double capacity) {
        this.locationProvider = locationProvider;
        this.capacity = capacity;
    }
    
    protected BasicEnergyTile(final World world, final BlockPos pos, final double capacity) {
        if (world == null) {
            throw new NullPointerException("null world");
        }
        if (pos == null) {
            throw new NullPointerException("null pos");
        }
        this.locationProvider = null;
        this.world = world;
        this.pos = pos;
        this.capacity = capacity;
    }
    
    public void update() {
        if (!this.addedToEnet) {
            this.onLoad();
        }
    }
    
    public void onLoad() {
        if (!this.addedToEnet && !this.getWorldObj().isRemote && Info.isIc2Available()) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileLoadEvent(this));
            this.addedToEnet = true;
        }
    }
    
    public void invalidate() {
        this.onChunkUnload();
    }
    
    public void onChunkUnload() {
        if (this.addedToEnet && !this.getWorldObj().isRemote && Info.isIc2Available()) {
            MinecraftForge.EVENT_BUS.post((Event)new EnergyTileUnloadEvent(this));
            this.addedToEnet = false;
        }
    }
    
    public void readFromNBT(final NBTTagCompound tag) {
        final NBTTagCompound data = tag.getCompoundTag(this.getNbtTagName());
        this.setEnergyStored(data.getDouble("energy"));
    }
    
    public NBTTagCompound writeToNBT(final NBTTagCompound tag) {
        final NBTTagCompound data = new NBTTagCompound();
        data.setDouble("energy", this.getEnergyStored());
        tag.setTag(this.getNbtTagName(), (NBTBase)data);
        return tag;
    }
    
    public double getCapacity() {
        return this.capacity;
    }
    
    public void setCapacity(final double capacity) {
        this.capacity = capacity;
    }
    
    public double getEnergyStored() {
        return this.energyStored;
    }
    
    public void setEnergyStored(final double amount) {
        this.energyStored = amount;
    }
    
    public double getFreeCapacity() {
        return this.getCapacity() - this.getEnergyStored();
    }
    
    public double addEnergy(double amount) {
        if (this.getWorldObj().isRemote) {
            return 0.0;
        }
        final double energyStored = this.getEnergyStored();
        final double capacity = this.getCapacity();
        if (amount > capacity - energyStored) {
            amount = capacity - energyStored;
        }
        this.setEnergyStored(energyStored + amount);
        return amount;
    }
    
    public boolean canUseEnergy(final double amount) {
        return this.getEnergyStored() >= amount;
    }
    
    public boolean useEnergy(final double amount) {
        if (!this.canUseEnergy(amount) || this.getWorldObj().isRemote) {
            return false;
        }
        this.setEnergyStored(this.getEnergyStored() - amount);
        return true;
    }
    
    public boolean charge(final ItemStack stack) {
        if (stack == null || !Info.isIc2Available() || this.getWorldObj().isRemote) {
            return false;
        }
        final double energyStored = this.getEnergyStored();
        final double amount = ElectricItem.manager.charge(stack, energyStored, Math.max(this.getSinkTier(), this.getSourceTier()), false, false);
        this.setEnergyStored(energyStored - amount);
        return amount > 0.0;
    }
    
    public boolean discharge(final ItemStack stack, final double limit) {
        if (stack == null || !Info.isIc2Available() || this.getWorldObj().isRemote) {
            return false;
        }
        final double energyStored = this.getEnergyStored();
        double amount = this.getCapacity() - energyStored;
        if (amount <= 0.0) {
            return false;
        }
        if (limit > 0.0 && limit < amount) {
            amount = limit;
        }
        amount = ElectricItem.manager.discharge(stack, amount, Math.max(this.getSinkTier(), this.getSourceTier()), limit > 0.0, true, false);
        this.setEnergyStored(energyStored + amount);
        return amount > 0.0;
    }
    
    @Override
    public World getWorldObj() {
        if (this.world == null) {
            this.initLocation();
        }
        return this.world;
    }
    
    @Override
    public BlockPos getPosition() {
        if (this.pos == null) {
            this.initLocation();
        }
        return this.pos;
    }
    
    private void initLocation() {
        if (this.locationProvider instanceof ILocatable) {
            final ILocatable provider = (ILocatable)this.locationProvider;
            this.world = provider.getWorldObj();
            this.pos = provider.getPosition();
        }
        else {
            if (!(this.locationProvider instanceof TileEntity)) {
                throw new IllegalStateException("no/incompatible location provider");
            }
            final TileEntity provider2 = (TileEntity)this.locationProvider;
            this.world = provider2.getWorld();
            this.pos = provider2.getPos();
        }
    }
    
    protected abstract String getNbtTagName();
    
    protected int getSinkTier() {
        return 0;
    }
    
    protected int getSourceTier() {
        return 0;
    }
}
