package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropCocoa extends IC2CropCard {
   @Override
   public String getId() {
      return "cocoa";
   }

   @Override
   public String getDiscoveredBy() {
      return "Notch";
   }

   @Override
   public CropProperties getProperties() {
      return new CropProperties(3, 1, 3, 0, 4, 0);
   }

   @Override
   public String[] getAttributes() {
      return new String[]{"Brown", "Food", "Stem"};
   }

   @Override
   public int getMaxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() <= 3 && crop.getStorageNutrients() >= 3;
   }

   @Override
   public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
      return (int)(humidity * 0.8 + nutrients * 1.3 + air * 0.9);
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() == 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.DYE, 1, 3);
   }

   @Override
   public int getGrowthDuration(ICropTile crop) {
      return crop.getCurrentSize() == 3 ? 900 : 400;
   }

   @Override
   public int getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }
}
