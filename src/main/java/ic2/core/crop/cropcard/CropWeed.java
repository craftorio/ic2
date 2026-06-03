package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CropWeed extends IC2CropCard {
  public String getId() {
    return "weed";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(0, 0, 0, 1, 0, 5);
  }
  
  public String[] getAttributes() {
    return new String[] { "Weed", "Bad" };
  }
  
  public int getMaxSize() {
    return 5;
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 1;
  }
  
  public boolean onLeftClick(ICropTile crop, EntityPlayer player) {
    return false;
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return false;
  }
  
  public ItemStack getGain(ICropTile crop) {
    return null;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    return 300;
  }
  
  public boolean onEntityCollision(ICropTile crop, Entity entity) {
    return false;
  }
}
