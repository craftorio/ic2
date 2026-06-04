// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.api.info.ILocatable;
import ic2.api.energy.EnergyNet;
import net.minecraft.tileentity.TileEntity;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.energy.tile.IEnergySink;

public abstract class BasicSinkSource extends BasicEnergyTile implements IEnergySink, IEnergySource
{
    protected int sinkTier;
    protected int sourceTier;
    
    public BasicSinkSource(final TileEntity parent, final double capacity, final int sinkTier, final int sourceTier) {
        super(parent, capacity);
        if (sinkTier < 0) {
            throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
        }
        if (sourceTier < 0) {
            throw new IllegalArgumentException("invalid source tier: " + sourceTier);
        }
        this.sinkTier = sinkTier;
        this.sourceTier = sourceTier;
        final double power = EnergyNet.instance.getPowerFromTier(sourceTier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public BasicSinkSource(final ILocatable parent, final double capacity, final int sinkTier, final int sourceTier) {
        super(parent, capacity);
        if (sinkTier < 0) {
            throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
        }
        if (sourceTier < 0) {
            throw new IllegalArgumentException("invalid source tier: " + sourceTier);
        }
        this.sinkTier = sinkTier;
        this.sourceTier = sourceTier;
        final double power = EnergyNet.instance.getPowerFromTier(sourceTier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public BasicSinkSource(final World world, final BlockPos pos, final double capacity, final int sinkTier, final int sourceTier) {
        super(world, pos, capacity);
        if (sinkTier < 0) {
            throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
        }
        if (sourceTier < 0) {
            throw new IllegalArgumentException("invalid source tier: " + sourceTier);
        }
        this.sinkTier = sinkTier;
        this.sourceTier = sourceTier;
        final double power = EnergyNet.instance.getPowerFromTier(sourceTier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public void setSinkTier(final int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.sinkTier = tier;
    }
    
    public void setSourceTier(final int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        final double power = EnergyNet.instance.getPowerFromTier(tier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
        this.sourceTier = tier;
    }
    
    @Override
    public double getDemandedEnergy() {
        return Math.max(0.0, this.getCapacity() - this.getEnergyStored());
    }
    
    @Override
    public double injectEnergy(final EnumFacing directionFrom, final double amount, final double voltage) {
        this.setEnergyStored(this.getEnergyStored() + amount);
        return 0.0;
    }
    
    @Override
    public int getSinkTier() {
        return this.sinkTier;
    }
    
    @Override
    public double getOfferedEnergy() {
        return this.getEnergyStored();
    }
    
    @Override
    public void drawEnergy(final double amount) {
        this.setEnergyStored(this.getEnergyStored() - amount);
    }
    
    @Override
    public int getSourceTier() {
        return this.sourceTier;
    }
    
    @Override
    protected String getNbtTagName() {
        return "IC2BasicSinkSource";
    }
}
