// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.energy.prefab;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import ic2.api.energy.tile.IEnergyEmitter;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import ic2.api.energy.tile.IEnergySink;

public class BasicSink extends BasicEnergyTile implements IEnergySink
{
    protected int tier;
    
    public BasicSink(final TileEntity parent, final double capacity, final int tier) {
        super(parent, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
    }
    
    public BasicSink(final ILocatable parent, final double capacity, final int tier) {
        super(parent, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
    }
    
    public BasicSink(final World world, final BlockPos pos, final double capacity, final int tier) {
        super(world, pos, capacity);
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
    }
    
    public void setSinkTier(final int tier) {
        if (tier < 0) {
            throw new IllegalArgumentException("invalid tier: " + tier);
        }
        this.tier = tier;
    }
    
    @Override
    public boolean acceptsEnergyFrom(final IEnergyEmitter emitter, final EnumFacing direction) {
        return true;
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
        return this.tier;
    }
    
    @Override
    protected String getNbtTagName() {
        return "IC2BasicSink";
    }
}
