package ic2.core.block;

import ic2.core.PointExplosion;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.IProjectile;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntityDynamite extends Entity implements IProjectile {
   public boolean sticky = false;
   public static final int netId = 142;
   public BlockPos stickPos;
   public int fuse = 100;
   private boolean inGround = false;
   public EntityLivingBase owner;
   private int ticksInGround;

   public EntityDynamite(World world, double x, double y, double z) {
      super(world);
      this.setSize(0.5F, 0.5F);
      this.setPosition(x, y, z);
   }

   public EntityDynamite(World world) {
      this(world, 0.0, 0.0, 0.0);
   }

   public EntityDynamite(World world, EntityLivingBase owner) {
      super(world);
      this.owner = owner;
      this.setSize(0.5F, 0.5F);
      Vector3 eyePos = Util.getEyePosition(owner);
      this.setLocationAndAngles(eyePos.x, eyePos.y, eyePos.z, owner.rotationYaw, owner.rotationPitch);
      this.posX = this.posX - Math.cos(Math.toRadians(this.rotationYaw)) * 0.16;
      this.posY -= 0.1;
      this.posZ = this.posZ - Math.sin(Math.toRadians(this.rotationYaw)) * 0.16;
      this.setPosition(this.posX, this.posY, this.posZ);
      this.motionX = -Math.sin(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
      this.motionZ = Math.cos(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
      this.motionY = -Math.sin(Math.toRadians(this.rotationPitch));
      this.shoot(this.motionX, this.motionY, this.motionZ, 1.0F, 1.0F);
   }

   protected void entityInit() {
   }

   public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
      double len = Math.sqrt(x * x + y * y + z * z);
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
      double hLen = Math.sqrt(x * x + z * z);
      this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0 / Math.PI);
      this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(y, hLen) * 180.0 / Math.PI);
      this.ticksInGround = 0;
   }

   public void setVelocity(double x, double y, double z) {
      this.motionX = x;
      this.motionY = y;
      this.motionZ = z;
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         double h = Math.sqrt(x * x + z * z);
         this.prevRotationYaw = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0 / Math.PI);
         this.prevRotationPitch = this.rotationPitch = (float)(Math.atan2(y, h) * 180.0 / Math.PI);
         this.prevRotationPitch = this.rotationPitch;
         this.prevRotationYaw = this.rotationYaw;
         this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
         this.ticksInGround = 0;
      }
   }

   public void onUpdate() {
      super.onUpdate();
      if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
         double hLen = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
         this.prevRotationYaw = this.rotationYaw = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
         this.prevRotationPitch = this.rotationPitch = (float)Math.toDegrees(Math.atan2(this.motionY, hLen));
      }

      World world = this.getEntityWorld();
      if (this.fuse-- <= 0) {
         this.setDead();
         if (!world.isRemote) {
            this.explode();
         }
      } else if (this.fuse < 100 && this.fuse % 2 == 0) {
         world.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0, new int[0]);
      }

      if (this.inGround) {
         this.ticksInGround++;
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

      Vec3d start = this.getPositionVector();
      Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
      RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
      if (result != null) {
         float remainX = (float)(result.hitVec.x - this.posX);
         float remainY = (float)(result.hitVec.y - this.posY);
         float remainZ = (float)(result.hitVec.z - this.posZ);
         double remDist = Math.sqrt(remainX * remainX + remainY * remainY + remainZ * remainZ);
         this.stickPos = result.getBlockPos();
         this.posX -= remainX / remDist * 0.05;
         this.posY -= remainY / remDist * 0.05;
         this.posZ -= remainZ / remDist * 0.05;
         this.posX += remainX;
         this.posY += remainY;
         this.posZ += remainZ;
         this.motionX = this.motionX * (0.75F - this.rand.nextFloat());
         this.motionY *= -0.3F;
         this.motionZ = this.motionZ * (0.75F - this.rand.nextFloat());
         this.inGround = true;
      } else {
         this.posX = this.posX + this.motionX;
         this.posY = this.posY + this.motionY;
         this.posZ = this.posZ + this.motionZ;
         this.inGround = false;
      }

      double hMotion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.rotationYaw = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
      this.rotationPitch = (float)Math.toDegrees(Math.atan2(this.motionY, hMotion));

      while (this.rotationPitch - this.prevRotationPitch < -180.0F) {
         this.prevRotationPitch -= 360.0F;
      }

      while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
         this.prevRotationPitch += 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
         this.prevRotationYaw -= 360.0F;
      }

      while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
         this.prevRotationYaw += 360.0F;
      }

      this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
      this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;
      float f3 = 0.98F;
      float f5 = 0.04F;
      if (this.isInWater()) {
         this.fuse += 2000;

         for (int i1 = 0; i1 < 4; i1++) {
            float f6 = 0.25F;
            world.spawnParticle(
               EnumParticleTypes.WATER_BUBBLE,
               this.posX - this.motionX * f6,
               this.posY - this.motionY * f6,
               this.posZ - this.motionZ * f6,
               this.motionX,
               this.motionY,
               this.motionZ,
               new int[0]
            );
         }

         f3 = 0.75F;
      }

      this.motionX *= f3;
      this.motionY *= f3;
      this.motionZ *= f3;
      this.motionY -= f5;
      this.setPosition(this.posX, this.posY, this.posZ);
   }

   public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
      nbttagcompound.setByte("inGround", (byte)(this.inGround ? 1 : 0));
   }

   public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
      this.inGround = nbttagcompound.getByte("inGround") == 1;
   }

   public void explode() {
      PointExplosion explosion = new PointExplosion(
         this.getEntityWorld(), this, this.owner, this.posX, this.posY, this.posZ, 1.0F, 1.0F, 20
      );
      explosion.doExplosionA();
      explosion.doExplosionB(true);
   }
}
