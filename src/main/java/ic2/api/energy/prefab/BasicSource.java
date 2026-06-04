// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IEnergyAcceptor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.api.info.ILocatable;
import ic2.api.energy.EnergyNet;
import net.minecraft.tileentity.TileEntity;
import ic2.api.energy.tile.IEnergySource;

public class BasicSource extends BasicEnergyTile implements IEnergySource
{
    protected int tier;
    
    public BasicSource(final TileEntity parent, final double capacity, final int tier) {
        super(parent, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
        final double power = EnergyNet.instance.getPowerFromTier(tier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public BasicSource(final ILocatable parent, final double capacity, final int tier) {
        super(parent, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
        final double power = EnergyNet.instance.getPowerFromTier(tier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public BasicSource(final World world, final BlockPos pos, final double capacity, final int tier) {
        super(world, pos, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
        final double power = EnergyNet.instance.getPowerFromTier(tier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
    }
    
    public void setSourceTier(final int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        final double power = EnergyNet.instance.getPowerFromTier(tier);
        if (this.getCapacity() < power) {
            this.setCapacity(power);
        }
        this.tier = tier;
    }
    
    @Override
    public boolean emitsEnergyTo(final IEnergyAcceptor receiver, final EnumFacing direction) {
        return true;
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
        return this.tier;
    }
    
    @Override
    protected String getNbtTagName() {
        return "IC2BasicSource";
    }
}
