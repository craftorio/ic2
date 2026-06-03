package ic2.core.item.tool;

import ic2.core.ref.IC2Material;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.block.material.Material;
import net.minecraft.init.Blocks;

public enum ToolClass implements IToolClass {
  Axe("axe", new Object[] { Material.field_151575_d, Material.field_151585_k, Material.field_151582_l }),
  Pickaxe("pickaxe", new Object[] { Material.field_151573_f, Material.field_151574_g, Material.field_151576_e }),
  Shears("shears", new Object[] { Blocks.field_150321_G, Blocks.field_150325_L, Blocks.field_150488_af, Blocks.field_150473_bD, Material.field_151584_j }),
  Shovel("shovel", new Object[] { Blocks.field_150431_aC, Blocks.field_150433_aE }),
  Sword("sword", new Object[] { Blocks.field_150321_G, Material.field_151585_k, Material.field_151582_l, Material.field_151589_v, Material.field_151584_j, Material.field_151572_C }),
  Hoe(null, new Object[] { Blocks.field_150346_d, Blocks.field_150349_c, Blocks.field_150391_bh }),
  Wrench("wrench", new Object[] { IC2Material.MACHINE, IC2Material.PIPE }),
  WireCutter("wire_cutter", new Object[] { IC2Material.CABLE }),
  Crowbar("crowbar", new Object[] { Blocks.field_150448_aq, Blocks.field_150408_cc, Blocks.field_150319_E, Blocks.field_150318_D });
  
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
