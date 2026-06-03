package ic2.core.item.tool;

import net.minecraft.item.Item;

public enum HarvestLevel {
  Wood(0, Item.ToolMaterial.WOOD),
  Stone(1, Item.ToolMaterial.STONE),
  Iron(2, Item.ToolMaterial.IRON),
  Diamond(3, Item.ToolMaterial.DIAMOND),
  Iridium(100, Item.ToolMaterial.DIAMOND);
  
  public final int level;
  
  public final Item.ToolMaterial toolMaterial;
  
  HarvestLevel(int level, Item.ToolMaterial toolMaterial) {
    this.level = level;
    this.toolMaterial = toolMaterial;
  }
}
