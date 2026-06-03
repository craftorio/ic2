package ic2.api.crops;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public enum CropSoilType {
  FARMLAND(Blocks.field_150458_ak),
  MYCELIUM((Block)Blocks.field_150391_bh),
  SAND((Block)Blocks.field_150354_m),
  SOULSAND(Blocks.field_150425_aM);
  
  private final Block block;
  
  CropSoilType(Block block) {
    this.block = block;
  }
  
  public Block getBlock() {
    return this.block;
  }
  
  public static boolean contais(Block block) {
    for (CropSoilType aux : values()) {
      if (aux.getBlock() == block)
        return true; 
    } 
    return false;
  }
}
