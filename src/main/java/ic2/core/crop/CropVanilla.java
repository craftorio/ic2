package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.ItemStack;

public abstract class CropVanilla extends IC2CropCard {
  protected final int maxAge;
  
  protected CropVanilla(BlockCrops block) {
    this(block.getMaxAge());
  }
  
  protected CropVanilla(int maxAge) {
    this.maxAge = maxAge;
  }
  
  public String getDiscoveredBy() {
    return "Notch";
  }
  
  public int getMaxSize() {
    return this.maxAge;
  }
  
  public boolean canGrow(ICropTile crop) {
    return (crop.getCurrentSize() < getMaxSize() && crop.getLightLevel() >= 9);
  }
  
  protected abstract ItemStack getSeeds();
  
  protected abstract ItemStack getProduct();
  
  public ItemStack getGain(ICropTile crop) {
    return getProduct();
  }
  
  public ItemStack getSeeds(ICropTile crop) {
    if (crop.getStatGain() <= 1 && crop.getStatGrowth() <= 1 && crop.getStatResistance() <= 1)
      return getSeeds(); 
    return super.getSeeds(crop);
  }
}
