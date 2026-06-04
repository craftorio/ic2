package ic2.core.item.tool;

import ic2.core.ref.IC2Material;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

public enum ToolClass implements IToolClass {
  Axe("axe", new Object[] { Material.WOOD, Material.PLANTS, Material.VINE }),
  Pickaxe("pickaxe", new Object[] { Material.IRON, Material.ANVIL, Material.ROCK }),
  Shears("shears", new Object[] { Blocks.WEB, Blocks.WOOL, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE, Material.LEAVES }),
  Shovel("shovel", new Object[] { Blocks.SNOW_LAYER, Blocks.SNOW }),
  Sword("sword", new Object[] { Blocks.WEB, Material.PLANTS, Material.VINE, Material.CORAL, Material.LEAVES, Material.GOURD }),
  Hoe(null, new Object[] { Blocks.DIRT, Blocks.GRASS, Blocks.MYCELIUM }),
  Wrench("wrench", new Object[] { IC2Material.MACHINE, IC2Material.PIPE }),
  WireCutter("wire_cutter", new Object[] { IC2Material.CABLE }),
  Crowbar("crowbar", new Object[] { Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL });
  
  public final String name;
  
  public final Set<Object> whitelist;
  
  public final Set<Object> blacklist;
  
  ToolClass(String name, Object[] whitelist, Object[] blacklist) {
    this.name = name;
    this.whitelist = new HashSet(Arrays.asList(whitelist));
    this.blacklist = new HashSet(Arrays.asList(blacklist));
  }
  
  public String getName() {
    return this.name;
  }
  
  public Set<Object> getWhitelist() {
    return this.whitelist;
  }
  
  public Set<Object> getBlacklist() {
    return this.blacklist;
  }
}
