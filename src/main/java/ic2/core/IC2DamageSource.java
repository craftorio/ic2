package ic2.core;

import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.world.Explosion;

public class IC2DamageSource extends DamageSource {
  public static IC2DamageSource electricity = new IC2DamageSource("electricity");
  
  public static IC2DamageSource nuke = (IC2DamageSource)(new IC2DamageSource("nuke")).func_94540_d();
  
  public static IC2DamageSource radiation = (IC2DamageSource)(new IC2DamageSource("radiation")).func_76348_h();
  
  public IC2DamageSource(String s) {
    super(s);
  }
  
  public static DamageSource getNukeSource(Explosion explosion) {
    return (explosion != null && explosion.func_94613_c() != null) ? (new EntityDamageSource("nuke.player", (Entity)explosion.func_94613_c())).func_94540_d() : nuke;
  }
}
