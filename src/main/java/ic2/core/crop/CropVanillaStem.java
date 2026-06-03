package ic2.core.crop;

import ic2.api.crops.ICropTile;

public abstract class CropVanillaStem extends CropVanilla {
  protected CropVanillaStem(int maxAge) {
    super(maxAge);
  }
  
  public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
    return (int)(humidity * 1.1D + nutrients * 0.9D + air);
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return this.maxAge - 1;
  }
}
