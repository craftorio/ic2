package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropBaseSapling extends IC2CropCard {
  protected final String cropName;
  
  protected final String saplingName;
  
  protected final ItemStack cropDrop;
  
  protected final ItemStack cropSapling;
  
  public CropBaseSapling(String cropName, String saplingName, ItemStack cropDrop, ItemStack cropSapling) {
    this.cropName = cropName;
    this.saplingName = "ic2.crop." + saplingName;
    this.cropDrop = cropDrop;
    this.cropSapling = cropSapling;
  }
  
  public String getId() {
    return this.cropName;
  }
  
  public String getSeedType() {
    return this.saplingName;
  }
  
  public String getDiscoveredBy() {
    return "Speiger";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(3, 1, 0, 4, 4, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Leaves", "Sapling", "Green" };
  }
  
  public int getMaxSize() {
    return 5;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < getMaxSize() && crop.getLightLevel() >= 9);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() == 5);
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 5;
  }
  
  public ItemStack[] getGains(ICropTile crop) {
    List<ItemStack> drops = new ArrayList<>();
    drops.add(this.cropDrop.copy());
    if (IC2.random.nextInt(100) >= 75)
      drops.add(this.cropSapling.copy()); 
    if (getId().equalsIgnoreCase("oak_sapling") && 
      IC2.random.nextInt(100) >= 75)
      drops.add(new ItemStack(Items.APPLE)); 
    return drops.<ItemStack>toArray(new ItemStack[drops.size()]);
  }
  
  public int getGrowthDuration(ICropTile crop) {
    return (crop.getCurrentSize() >= 4) ? 150 : 600;
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 4;
  }
}
