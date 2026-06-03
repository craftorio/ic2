package ic2.api.energy.prefab;

import ic2.api.energy.tile.IEnergyEmitter;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.info.ILocatable;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BasicSink extends BasicEnergyTile implements IEnergySink {
  protected int tier;
  
  public BasicSink(TileEntity parent, double capacity, int tier) {
    super(parent, capacity);
    if (tier < 0)
      throw new IllegalArgumentException("invalid tier: " + tier); 
    this.tier = tier;
  }
  
  public BasicSink(ILocatable parent, double capacity, int tier) {
    super(parent, capacity);
    if (tier < 0)
      throw new IllegalArgumentException("invalid tier: " + tier); 
    this.tier = tier;
  }
  
  public BasicSink(World world, BlockPos pos, double capacity, int tier) {
    super(world, pos, capacity);
    if (tier < 0)
      throw new IllegalArgumentException("invalid tier: " + tier); 
    this.tier = tier;
  }
  
  public void setSinkTier(int tier) {
    if (tier < 0)
      throw new IllegalArgumentException("invalid tier: " + tier); 
    this.tier = tier;
  }
  
  public boolean acceptsEnergyFrom(IEnergyEmitter emitter, EnumFacing direction) {
    return true;
  }
  
  public double getDemandedEnergy() {
    return Math.max(0.0D, getCapacity() - getEnergyStored());
  }
  
  public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
    setEnergyStored(getEnergyStored() + amount);
    return 0.0D;
  }
  
  public int getSinkTier() {
    return this.tier;
  }
  
  protected String getNbtTagName() {
    return "IC2BasicSink";
  }
}
