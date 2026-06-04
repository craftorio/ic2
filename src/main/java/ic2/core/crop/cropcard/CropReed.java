package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropReed extends IC2CropCard {
  public String getId() {
    return "reed";
  }
  
  public String getDiscoveredBy() {
    return "Notch";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(2, 0, 0, 1, 0, 2);
  }
  
  public String[] getAttributes() {
    return new String[] { "Reed" };
  }
  
  public int getMaxSize() {
    return 3;
  }
  
  public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
    return (int)(humidity * 1.2D + nutrients + air * 0.8D);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() > 1);
  }
  
  public ItemStack getGain(ICropTile crop) {
    return new ItemStack(Items.REEDS, crop.getCurrentSize() - 1);
  }
  
  public boolean onEntityCollision(ICropTile crop, Entity entity) {
    return false;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    return 200;
  }
}
