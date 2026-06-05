package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CropRedWheat extends IC2CropCard {
   @Override
   public String getId() {
      return "redwheat";
   }

   @Override
   public String getDiscoveredBy() {
      return "raa1337";
   }

   @Override
   public CropProperties getProperties() {
      return new CropProperties(6, 3, 0, 0, 2, 0);
   }

   @Override
   public String[] getAttributes() {
      return new String[]{"Red", "Redstone", "Wheat"};
   }

   @Override
   public int getMaxSize() {
      return 7;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() < 7 && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() == 7;
   }

   @Override
   public double dropGainChance() {
      return 0.5;
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 7;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      BlockPos coords = crop.getPosition();
      return crop.getWorldObj().isBlockIndirectlyGettingPowered(coords) <= 0 && !crop.getWorldObj().rand.nextBoolean()
         ? new ItemStack(Items.WHEAT, 1)
         : new ItemStack(Items.REDSTONE, 1);
   }

   @Override
   public boolean isRedstoneSignalEmitter(ICropTile crop) {
      return true;
   }

   @Override
   public int getEmittedRedstoneSignal(ICropTile crop) {
      return crop.getCurrentSize() == 7 ? 15 : 0;
   }

   @Override
   public int getEmittedLight(ICropTile crop) {
      return crop.getCurrentSize() == 7 ? 7 : 0;
   }

   @Override
   public int getGrowthDuration(ICropTile crop) {
      return 600;
   }

   @Override
   public int getSizeAfterHarvest(ICropTile crop) {
      return 2;
   }
}
