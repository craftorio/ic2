package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import ic2.core.crop.IC2Crops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropNetherWart extends IC2CropCard {
  public String getId() {
    return "nether_wart";
  }
  
  public String getDiscoveredBy() {
    return "Notch";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(5, 4, 2, 0, 2, 1);
  }
  
  public String[] getAttributes() {
    return new String[] { "Red", "Nether", "Ingredient", "Soulsand" };
  }
  
  public int getMaxSize() {
    return 3;
  }
  
  public double dropGainChance() {
    return 2.0D;
  }
  
  public ItemStack getGain(ICropTile crop) {
    return new ItemStack(Items.field_151075_bm, 1);
  }
  
  public void tick(ICropTile crop) {
    if (crop.isBlockBelow(Blocks.SOUL_SAND)) {
      if (canGrow(crop))
        crop.setGrowthPoints(crop.getGrowthPoints() + 100); 
    } else if (crop.isBlockBelow(Blocks.field_150433_aE) && (crop.getWorldObj()).rand.nextInt(300) == 0) {
      crop.setCrop(IC2Crops.cropTerraWart);
    } 
  }
  
  public int getRootsLength(ICropTile crop) {
    return 5;
  }
}
