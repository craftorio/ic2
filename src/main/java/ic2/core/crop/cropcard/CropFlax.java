package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropFlax extends IC2CropCard {
  public String getId() {
    return "flax";
  }
  
  public String getDiscoveredBy() {
    return "Eloraam";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(2, 1, 1, 2, 0, 1);
  }
  
  public String[] getAttributes() {
    return new String[] { "Silk", "Vine", "Addictive" };
  }
  
  public int getMaxSize() {
    return 4;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < 4 && crop.getLightLevel() >= 9);
  }
  
  public ItemStack getGain(ICropTile crop) {
    return new ItemStack(Items.field_151007_F);
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 1;
  }
}
