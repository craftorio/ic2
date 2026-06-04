package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import ic2.core.crop.IC2Crops;
import ic2.core.ref.ItemName;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;

public class CropTerraWart extends IC2CropCard {
  public String getId() {
    return "terra_wart";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(5, 2, 4, 0, 3, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Blue", "Aether", "Consumable", "Snow" };
  }
  
  public int getMaxSize() {
    return 3;
  }
  
  public double dropGainChance() {
    return 0.8D;
  }
  
  public ItemStack getGain(ICropTile crop) {
    return ItemName.terra_wart.getItemStack();
  }
  
  public void tick(ICropTile crop) {
    if (crop.isBlockBelow(Blocks.SNOW)) {
      if (canGrow(crop))
        crop.setGrowthPoints(crop.getGrowthPoints() + 100); 
    } else if (crop.isBlockBelow(Blocks.SOUL_SAND) && (crop.getWorldObj()).rand.nextInt(300) == 0) {
      crop.setCrop(IC2Crops.cropNetherWart);
    } 
  }
  
  public int getRootsLength(ICropTile crop) {
    return 5;
  }
}
