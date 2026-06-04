package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.CropVanillaStem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropMelon extends CropVanillaStem {
  public CropMelon() {
    super(4);
  }
  
  public String getId() {
    return "melon";
  }
  
  public String getDiscoveredBy() {
    return "Chao";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(2, 0, 4, 0, 2, 0);
  }
  
  public String[] getAttributes() {
    return new String[] { "Green", "Food", "Stem" };
  }
  
  protected ItemStack getProduct() {
    if (IC2.random.nextInt(3) == 0)
      return new ItemStack(Blocks.MELON_BLOCK); 
    return new ItemStack(Items.MELON, IC2.random.nextInt(4) + 2);
  }
  
  protected ItemStack getSeeds() {
    return new ItemStack(Items.MELON_SEEDS, IC2.random.nextInt(2) + 1);
  }
  
  public int getGrowthDuration(ICropTile crop) {
    if (crop.getCurrentSize() == 3)
      return 700; 
    return 250;
  }
}
