package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.CropVanillaStem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropPumpkin extends CropVanillaStem {
  public CropPumpkin() {
    super(4);
  }
  
  public String getId() {
    return "pumpkin";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(1, 0, 1, 0, 3, 1);
  }
  
  public String[] getAttributes() {
    return new String[] { "Orange", "Decoration", "Stem" };
  }
  
  protected ItemStack getProduct() {
    return new ItemStack(Blocks.PUMPKIN);
  }
  
  protected ItemStack getSeeds() {
    return new ItemStack(Items.PUMPKIN_SEEDS, IC2.random.nextInt(3) + 1);
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() == 3)
      return 600; 
    return 200;
  }
}
