package ic2.core.ref;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class IC2Material extends Material {
  public static final IC2Material MACHINE = new IC2Material("ic2_material_machine", true, true);
  
  public static final IC2Material PIPE = new IC2Material("ic2_material_pipe", true, true);
  
  public static final IC2Material CABLE = new IC2Material("ic2_material_cable", false, true);
  
  public final String name;
  
  public IC2Material(String name, boolean requiresTool, boolean immovableMobility) {
    super(MapColor.IRON);
    this.name = name;
    if (requiresTool)
      setRequiresTool(); 
    if (immovableMobility)
      setImmovableMobility(); 
    setAdventureModeExempt();
  }
}
