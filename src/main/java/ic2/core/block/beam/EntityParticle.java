package ic2.core.block.beam;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;

public class EntityParticle extends Entity {
   private static final double initialVelocity = 0.5;
   private static final double slowdown = 0.99;

   public EntityParticle(World world) {
      super(world);
      this.noClip = true;
   }

   public EntityParticle(TileEmitter emitter) {
      this(emitter.getWorld());
      EnumFacing dir = emitter.getFacing();
      double x = emitter.getPos().getX() + 0.5 + dir.getFrontOffsetX() * 0.5;
      double y = emitter.getPos().getY() + 0.5 + dir.getFrontOffsetY() * 0.5;
      double z = emitter.getPos().getZ() + 0.5 + dir.getFrontOffsetZ() * 0.5;
      this.setPosition(x, y, z);
      this.motionX = dir.getFrontOffsetX() * 0.5;
      this.motionY = dir.getFrontOffsetY() * 0.5;
      this.motionZ = dir.getFrontOffsetZ() * 0.5;
      this.setSize(0.2F, 0.2F);
   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
      this.motionX *= 0.99;
      this.motionY *= 0.99;
      this.motionZ *= 0.99;
      if (this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ < 1.0E-4) {
         this.setDead();
      }
   }
}
