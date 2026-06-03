package ic2.core.util;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class EntityIC2FX extends Particle {
  public EntityIC2FX(World world, double x, double y, double z, int maxAge, double[] velocity, float[] colour) {
    super(world, x, y, z, velocity[0], velocity[1], velocity[2]);
    this.field_70552_h = colour[0];
    this.field_70553_i = colour[1];
    this.field_70551_j = colour[2];
    this.field_70545_g = 0.0F;
    this.field_82339_as = 0.6F;
    this.field_70547_e = maxAge;
    func_187115_a(0.02F, 0.02F);
    this.field_70544_f *= this.field_187136_p.nextFloat() * 0.6F + 0.5F;
  }
  
  public void func_189213_a() {
    this.field_187123_c = this.field_187126_f;
    this.field_187124_d = this.field_187127_g;
    this.field_187125_e = this.field_187128_h;
    func_187110_a(0.0D, 0.019999999552965164D, 0.0D);
    if (this.field_70546_d++ >= this.field_70547_e)
      func_187112_i(); 
  }
}
