// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.ref.IC2Material;
import net.minecraft.init.Blocks;
import net.minecraft.block.material.Material;
import java.util.Collection;
import java.util.HashSet;
import java.util.Arrays;
import java.util.Set;

public enum ToolClass implements IToolClass
{
    Axe("axe", new Object[] { Material.WOOD, Material.PLANTS, Material.VINE }), 
    Pickaxe("pickaxe", new Object[] { Material.IRON, Material.ANVIL, Material.ROCK }), 
    Shears("shears", new Object[] { Blocks.WEB, Blocks.WOOL, Blocks.REDSTONE_WIRE, Blocks.TRIPWIRE, Material.LEAVES }), 
    Shovel("shovel", new Object[] { Blocks.SNOW_LAYER, Blocks.SNOW }), 
    Sword("sword", new Object[] { Blocks.WEB, Material.PLANTS, Material.VINE, Material.CORAL, Material.LEAVES, Material.GOURD }), 
    Hoe((String)null, new Object[] { Blocks.DIRT, Blocks.GRASS, Blocks.MYCELIUM }), 
    Wrench("wrench", new Object[] { IC2Material.MACHINE, IC2Material.PIPE }), 
    WireCutter("wire_cutter", new Object[] { IC2Material.CABLE }), 
    Crowbar("crowbar", new Object[] { Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL });
    
    public final String name;
    public final Set<Object> whitelist;
    public final Set<Object> blacklist;
    
    private ToolClass(final String name, final Object[] whitelist) {
        this(name, whitelist, new Object[0]);
    }
    
    private ToolClass(final String name, final Object[] whitelist, final Object[] blacklist) {
        this.name = name;
        this.whitelist = new HashSet<Object>(Arrays.asList(whitelist));
        this.blacklist = new HashSet<Object>(Arrays.asList(blacklist));
    }
    
    @Override
    public String getName() {
        return this.name;
    }
    
    @Override
    public Set<Object> getWhitelist() {
        return this.whitelist;
    }
    
    @Override
    public Set<Object> getBlacklist() {
        return this.blacklist;
    }
}
