package ic2.api.energy.prefab;

import ic2.api.energy.EnergyNet;
import ic2.api.energy.tile.IEnergySink;
import ic2.api.energy.tile.IEnergySource;
import ic2.api.info.ILocatable;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public abstract class BasicSinkSource extends BasicEnergyTile implements IEnergySink, IEnergySource {
   protected int sinkTier;
   protected int sourceTier;

   public BasicSinkSource(TileEntity parent, double capacity, int sinkTier, int sourceTier) {
      super(parent, capacity);
      if (sinkTier < 0) {
         throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
      }

      if (sourceTier < 0) {
         throw new IllegalArgumentException("invalid source tier: " + sourceTier);
      }

      this.sinkTier = sinkTier;
      this.sourceTier = sourceTier;
      double power = EnergyNet.instance.getPowerFromTier(sourceTier);
      if (this.getCapacity() < power) {
         this.setCapacity(power);
      }
   }

   public BasicSinkSource(ILocatable parent, double capacity, int sinkTier, int sourceTier) {
      super(parent, capacity);
      if (sinkTier < 0) {
         throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
      }

      if (sourceTier < 0) {
         throw new IllegalArgumentException("invalid source tier: " + sourceTier);
      }

      this.sinkTier = sinkTier;
      this.sourceTier = sourceTier;
      double power = EnergyNet.instance.getPowerFromTier(sourceTier);
      if (this.getCapacity() < power) {
         this.setCapacity(power);
      }
   }

   public BasicSinkSource(World world, BlockPos pos, double capacity, int sinkTier, int sourceTier) {
      super(world, pos, capacity);
      if (sinkTier < 0) {
         throw new IllegalArgumentException("invalid sink tier: " + sinkTier);
      }

      if (sourceTier < 0) {
         throw new IllegalArgumentException("invalid source tier: " + sourceTier);
      }

      this.sinkTier = sinkTier;
      this.sourceTier = sourceTier;
      double power = EnergyNet.instance.getPowerFromTier(sourceTier);
      if (this.getCapacity() < power) {
         this.setCapacity(power);
      }
   }

   public void setSinkTier(int tier) {
      if (tier < 0) {
         throw new IllegalArgumentException("invalid tier: " + tier);
      }

      this.sinkTier = tier;
   }

   public void setSourceTier(int tier) {
      if (tier < 0) {
         throw new IllegalArgumentException("invalid tier: " + tier);
      }

      double power = EnergyNet.instance.getPowerFromTier(tier);
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
   public double injectEnergy(EnumFacing directionFrom, double amount, double voltage) {
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
   public void drawEnergy(double amount) {
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
