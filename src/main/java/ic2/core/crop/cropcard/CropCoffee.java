package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class CropCoffee extends IC2CropCard {
  public String getId() {
    return "coffee";
  }
  
  public String getDiscoveredBy() {
    return "Snoochy";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(7, 1, 4, 1, 2, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Leaves", "Ingredient", "Beans" };
  }
  
  public int getMaxSize() {
    return 5;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < 5 && crop.getLightLevel() >= 9);
  }
  
  public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
    return (int)(0.4D * humidity + 1.4D * nutrients + 1.2D * air);
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() == 3)
      return (int)(super.getGrowthDuration(crop) * 0.5D); 
    if (crop.getCurrentSize() == 4)
      return (int)(super.getGrowthDuration(crop) * 1.5D); 
    return super.getGrowthDuration(crop);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() >= 4);
  }
  
  public ItemStack getGain(ICropTile crop) {
    if (crop.getCurrentSize() == 4)
      return null; 
    return ItemName.crop_res.getItemStack((Enum)CropResItemType.coffee_beans);
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 3;
  }
}
