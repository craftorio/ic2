package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;

public class CropRedWheat extends IC2CropCard {
  public String getId() {
    return "redwheat";
  }
  
  public String getDiscoveredBy() {
    return "raa1337";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(6, 3, 0, 0, 2, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Red", "Redstone", "Wheat" };
  }
  
  public int getMaxSize() {
    return 7;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < 7 && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5);
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() == 7);
  }
  
  public double dropGainChance() {
    return 0.5D;
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 7;
  }
  
  public ItemStack getGain(ICropTile crop) {
    BlockPos coords = crop.getPosition();
    if (crop.getWorldObj().func_175687_A(coords) > 0 || (crop.getWorldObj()).field_73012_v.nextBoolean())
      return new ItemStack(Items.field_151137_ax, 1); 
    return new ItemStack(Items.field_151015_O, 1);
  }
  
  public boolean isRedstoneSignalEmitter(ICropTile crop) {
    return true;
  }
  
  public int getEmittedRedstoneSignal(ICropTile crop) {
    return (crop.getCurrentSize() == 7) ? 15 : 0;
  }
  
  public int getEmittedLight(ICropTile crop) {
    return (crop.getCurrentSize() == 7) ? 7 : 0;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    return 600;
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 2;
  }
}
