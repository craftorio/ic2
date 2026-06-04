// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.ref;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class IC2Material extends Material
{
    public static final IC2Material MACHINE;
    public static final IC2Material PIPE;
    public static final IC2Material CABLE;
    public final String name;
    
    public IC2Material(final String name, final boolean requiresTool, final boolean immovableMobility) {
        super(MapColor.IRON);
        this.name = name;
        if (requiresTool) {
            this.setRequiresTool();
        }
        if (immovableMobility) {
            this.setImmovableMobility();
        }
        this.setAdventureModeExempt();
    }
    
    static {
        MACHINE = new IC2Material("ic2_material_machine", true, true);
        PIPE = new IC2Material("ic2_material_pipe", true, true);
        CABLE = new IC2Material("ic2_material_cable", false, true);
    }
}
