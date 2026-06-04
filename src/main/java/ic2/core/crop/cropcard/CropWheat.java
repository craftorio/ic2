package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.CropVanilla;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropWheat extends CropVanilla {
  public CropWheat() {
    super((BlockCrops)Blocks.WHEAT);
  }
  
  public String getId() {
    return "wheat";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(1, 0, 4, 0, 0, 2);
  }
  
  public String[] getAttributes() {
    return new String[] { "Yellow", "Food", "Wheat" };
  }
  
  public ItemStack getProduct() {
    return new ItemStack(Items.WHEAT, 1);
  }
  
  public ItemStack getSeeds() {
    return new ItemStack(Items.WHEAT_SEEDS);
  }
  
  public int getSizeAfterHarvest(ICropTile crop) {
    return 2;
  }
}
