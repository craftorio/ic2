// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import ic2.core.PointExplosion;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.EnumParticleTypes;
import ic2.core.util.Vector3;
import ic2.core.util.Util;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.BlockPos;
import net.minecraft.entity.IProjectile;
import net.minecraft.entity.Entity;

public class EntityDynamite extends Entity implements IProjectile
{
    public boolean sticky;
    public static final int netId = 142;
    public BlockPos stickPos;
    public int fuse;
    private boolean inGround;
    public EntityLivingBase owner;
    private int ticksInGround;
    
    public EntityDynamite(final World world, final double x, final double y, final double z) {
        super(world);
        this.sticky = false;
        this.fuse = 100;
        this.inGround = false;
        this.setSize(0.5f, 0.5f);
        this.setPosition(x, y, z);
    }
    
    public EntityDynamite(final World world) {
        this(world, 0.0, 0.0, 0.0);
    }
    
    public EntityDynamite(final World world, final EntityLivingBase owner) {
        super(world);
        this.sticky = false;
        this.fuse = 100;
        this.inGround = false;
        this.owner = owner;
        this.setSize(0.5f, 0.5f);
        final Vector3 eyePos = Util.getEyePosition((Entity)owner);
        this.setLocationAndAngles(eyePos.x, eyePos.y, eyePos.z, owner.rotationYaw, owner.rotationPitch);
        this.posX -= Math.cos(Math.toRadians(this.rotationYaw)) * 0.16;
        this.posY -= 0.1;
        this.posZ -= Math.sin(Math.toRadians(this.rotationYaw)) * 0.16;
        this.setPosition(this.posX, this.posY, this.posZ);
        this.motionX = -Math.sin(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
        this.motionZ = Math.cos(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
        this.motionY = -Math.sin(Math.toRadians(this.rotationPitch));
        this.shoot(this.motionX, this.motionY, this.motionZ, 1.0f, 1.0f);
    }
    
    protected void entityInit() {
    }
    
    public void shoot(double x, double y, double z, final float velocity, final float inaccuracy) {
        final double len = Math.sqrt(x * x + y * y + z * z);
        x /= len;
        y /= len;
        z /= len;
        x += this.rand.nextGaussian() * 0.0075 * inaccuracy;
        y += this.rand.nextGaussian() * 0.0075 * inaccuracy;
        z += this.rand.nextGaussian() * 0.0075 * inaccuracy;
        x *= velocity;
        y *= velocity;
        z *= velocity;
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        final double hLen = Math.sqrt(x * x + z * z);
        final float n = (float)(Math.atan2(x, z) * 180.0 / 3.141592653589793);
        this.rotationYaw = n;
        this.prevRotationYaw = n;
        final float n2 = (float)(Math.atan2(y, hLen) * 180.0 / 3.141592653589793);
        this.rotationPitch = n2;
        this.prevRotationPitch = n2;
        this.ticksInGround = 0;
    }
    
    public void setVelocity(final double x, final double y, final double z) {
        this.motionX = x;
        this.motionY = y;
        this.motionZ = z;
        if (this.prevRotationPitch == 0.0f && this.prevRotationYaw == 0.0f) {
            final double h = Math.sqrt(x * x + z * z);
            final float n = (float)(Math.atan2(x, z) * 180.0 / 3.141592653589793);
            this.rotationYaw = n;
            this.prevRotationYaw = n;
            final float n2 = (float)(Math.atan2(y, h) * 180.0 / 3.141592653589793);
            this.rotationPitch = n2;
            this.prevRotationPitch = n2;
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }
    }
    
    public void onUpdate() {
        super.onUpdate();
        if (this.prevRotationPitch == 0.0f && this.prevRotationYaw == 0.0f) {
            final double hLen = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
            final float n = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
            this.rotationYaw = n;
            this.prevRotationYaw = n;
            final float n2 = (float)Math.toDegrees(Math.atan2(this.motionY, hLen));
            this.rotationPitch = n2;
            this.prevRotationPitch = n2;
        }
        final World world = this.getEntityWorld();
        if (this.fuse-- <= 0) {
            this.setDead();
            if (!world.isRemote) {
                this.explode();
            }
        }
        else if (this.fuse < 100 && this.fuse % 2 == 0) {
            world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0, new int[0]);
        }
        if (this.inGround) {
            ++this.ticksInGround;
            if (this.ticksInGround >= 200) {
                this.setDead();
            }
            if (this.sticky) {
                this.fuse -= 3;
                this.motionX = 0.0;
                this.motionY = 0.0;
                this.motionZ = 0.0;
                if (!world.isAirBlock(this.stickPos)) {
                    return;
                }
            }
        }
        final Vec3d start = this.getPositionVector();
        final Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
        final RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
        if (result != null) {
            final float remainX = (float)(result.hitVec.x - this.posX);
            final float remainY = (float)(result.hitVec.y - this.posY);
            final float remainZ = (float)(result.hitVec.z - this.posZ);
            final double remDist = Math.sqrt(remainX * remainX + remainY * remainY + remainZ * remainZ);
            this.stickPos = result.getBlockPos();
            this.posX -= remainX / remDist * 0.05;
            this.posY -= remainY / remDist * 0.05;
            this.posZ -= remainZ / remDist * 0.05;
            this.posX += remainX;
            this.posY += remainY;
            this.posZ += remainZ;
            this.motionX *= 0.75f - this.rand.nextFloat();
            this.motionY *= -0.30000001192092896;
            this.motionZ *= 0.75f - this.rand.nextFloat();
            this.inGround = true;
        }
        else {
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;
            this.inGround = false;
        }
        final double hMotion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
        this.rotationYaw = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
        this.rotationPitch = (float)Math.toDegrees(Math.atan2(this.motionY, hMotion));
        while (this.rotationPitch - this.prevRotationPitch < -180.0f) {
            this.prevRotationPitch -= 360.0f;
        }
        while (this.rotationPitch - this.prevRotationPitch >= 180.0f) {
            this.prevRotationPitch += 360.0f;
        }
        while (this.rotationYaw - this.prevRotationYaw < -180.0f) {
            this.prevRotationYaw -= 360.0f;
        }
        while (this.rotationYaw - this.prevRotationYaw >= 180.0f) {
            this.prevRotationYaw += 360.0f;
        }
        this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2f;
        this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2f;
        float f3 = 0.98f;
        final float f4 = 0.04f;
        if (this.isInWater()) {
            this.fuse += 2000;
            for (int i1 = 0; i1 < 4; ++i1) {
                final float f5 = 0.25f;
                world.spawnParticle(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f5, this.posY - this.motionY * f5, this.posZ - this.motionZ * f5, this.motionX, this.motionY, this.motionZ, new int[0]);
            }
            f3 = 0.75f;
        }
        this.motionX *= f3;
        this.motionY *= f3;
        this.motionZ *= f3;
        this.motionY -= f4;
        this.setPosition(this.posX, this.posY, this.posZ);
    }
    
    public void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("inGround", (byte)(byte)(this.inGround ? 1 : 0));
    }
    
    public void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
        this.inGround = (nbttagcompound.getByte("inGround") == 1);
    }
    
    public void explode() {
        final PointExplosion explosion = new PointExplosion(this.getEntityWorld(), this, this.owner, this.posX, this.posY, this.posZ, 1.0f, 1.0f, 20);
        explosion.doExplosionA();
        explosion.doExplosionB(true);
    }
}
