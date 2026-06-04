package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropCocoa extends IC2CropCard {
  public String getId() {
    return "cocoa";
  }
  
  public String getDiscoveredBy() {
    return "Notch";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(3, 1, 3, 0, 4, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Brown", "Food", "Stem" };
  }
  
  public int getMaxSize() {
    return 4;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() <= 3 && crop.getStorageNutrients() >= 3);
  }
  
  public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
    return (int)(humidity * 0.8D + nutrients * 1.3D + air * 0.9D);
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 4;
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() == 4);
  }
  
  public ItemStack getGain(ICropTile crop) {
    return new ItemStack(Items.DYE, 1, 3);
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() == 3)
      return 900; 
    return 400;
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 3;
  }
}
