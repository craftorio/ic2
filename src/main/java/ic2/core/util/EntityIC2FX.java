// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.util;

import net.minecraft.world.World;
import net.minecraft.client.particle.Particle;

public class EntityIC2FX extends Particle
{
    public EntityIC2FX(final World world, final double x, final double y, final double z, final int maxAge, final double[] velocity, final float[] colour) {
        super(world, x, y, z, velocity[0], velocity[1], velocity[2]);
        this.particleRed = colour[0];
        this.particleGreen = colour[1];
        this.particleBlue = colour[2];
        this.particleGravity = 0.0f;
        this.particleAlpha = 0.6f;
        this.particleMaxAge = maxAge;
        this.setSize(0.02f, 0.02f);
        this.particleScale *= this.rand.nextFloat() * 0.6f + 0.5f;
    }
    
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.move(0.0, 0.019999999552965164, 0.0);
        if (this.particleAge++ >= this.particleMaxAge) {
            this.setExpired();
        }
    }
}
