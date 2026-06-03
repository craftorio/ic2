package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropStickreed extends IC2CropCard {
  public String getId() {
    return "stickreed";
  }
  
  public String getDiscoveredBy() {
    return "raa1337";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(4, 2, 0, 1, 0, 1);
  }
  
  public String[] getAttributes() {
    return new String[] { "Reed", "Resin" };
  }
  
  public int getMaxSize() {
    return 4;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < 4);
  }
  
  public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
    return (int)(humidity * 1.2D + nutrients + air * 0.8D);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() > 1);
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 4;
  }
  
  public ItemStack getGain(ICropTile crop) {
    if (crop.getCurrentSize() <= 3)
      return new ItemStack(Items.field_151120_aE, crop.getCurrentSize() - 1); 
    return ItemName.misc_resource.getItemStack((Enum)MiscResourceType.resin);
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    if (crop.getCurrentSize() == 4)
      return (byte)(3 - IC2.random.nextInt(3)); 
    return 1;
  }
  
  public boolean onEntityCollision(ICropTile crop, Entity entity) {
    return false;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() == 4)
      return 400; 
    return 100;
  }
}
