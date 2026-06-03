package ic2.core.block;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;

public class MaterialIC2TNT extends Material {
  public static Material instance = new MaterialIC2TNT();
  
  public MaterialIC2TNT() {
    super(MapColor.field_151656_f);
    func_85158_p();
    func_76226_g();
  }
  
  public boolean func_76218_k() {
    return false;
  }
}
