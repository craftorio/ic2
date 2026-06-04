package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.core.crop.CropVanilla;
import net.minecraft.block.BlockCrops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropBeetroot extends CropVanilla {
  public CropBeetroot() {
    super((BlockCrops)Blocks.BEETROOTS);
  }
  
  public String getId() {
    return "beetroots";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(1, 0, 4, 0, 1, 2);
  }
  
  public String[] getAttributes() {
    return new String[] { "Red", "Food", "Beetroot" };
  }
  
  public ItemStack getProduct() {
    return new ItemStack(Items.BEETROOT, 1);
  }
  
  public ItemStack getSeeds() {
    return new ItemStack(Items.BEETROOT_SEEDS);
  }
}
