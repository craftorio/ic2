package ic2.core.util;

import net.minecraft.client.particle.Particle;
import net.minecraft.world.World;

public class EntityIC2FX extends Particle {
  public EntityIC2FX(World world, double x, double y, double z, int maxAge, double[] velocity, float[] colour) {
    super(world, x, y, z, velocity[0], velocity[1], velocity[2]);
    this.particleRed = colour[0];
    this.particleGreen = colour[1];
    this.particleBlue = colour[2];
    this.particleGravity = 0.0F;
    this.particleAlpha = 0.6F;
    this.particleMaxAge = maxAge;
    setSize(0.02F, 0.02F);
    this.particleScale *= this.rand.nextFloat() * 0.6F + 0.5F;
  }
  
  public void onUpdate() {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    move(0.0D, 0.019999999552965164D, 0.0D);
    if (this.particleAge++ >= this.particleMaxAge)
      setExpired(); 
  }
}
