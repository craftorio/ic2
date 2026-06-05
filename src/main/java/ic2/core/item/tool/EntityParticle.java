package ic2.core.item.tool;

import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import ic2.core.util.Quaternion;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import ic2.core.util.Vector3;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.IThrowableEntity;

public class EntityParticle extends Entity implements IThrowableEntity {
   private double coreSize;
   private double influenceSize;
   private int lifeTime;
   private Entity owner;
   private Vector3[] radialTestVectors;

   public EntityParticle(World world) {
      super(world);
      this.noClip = true;
      this.lifeTime = 6000;
   }

   public EntityParticle(World world, EntityLivingBase owner1, float speed, double coreSize1, double influenceSize1) {
      this(world);
      this.coreSize = coreSize1;
      this.influenceSize = influenceSize1;
      this.owner = owner1;
      Vector3 eyePos = Util.getEyePosition(this.owner);
      this.setPosition(eyePos.x, eyePos.y, eyePos.z);
      Vector3 motion = new Vector3(owner1.getLookVec());
      Vector3 ortho = motion.copy().cross(Vector3.UP).scaleTo(influenceSize1);
      double stepAngle = Math.atan(0.5 / influenceSize1) * 2.0;
      int steps = (int)Math.ceil((Math.PI * 2) / stepAngle);
      Quaternion q = new Quaternion().setFromAxisAngle(motion, stepAngle);
      this.radialTestVectors = new Vector3[steps];
      this.radialTestVectors[0] = ortho.copy();

      for (int i = 1; i < steps; i++) {
         q.rotate(ortho);
         this.radialTestVectors[i] = ortho.copy();
      }

      motion.scale(speed);
      this.motionX = motion.x;
      this.motionY = motion.y;
      this.motionZ = motion.z;
   }

   protected void entityInit() {
   }

   protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
   }

   protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
   }

   public Entity getThrower() {
      return this.owner;
   }

   public void setThrower(Entity entity) {
      this.owner = entity;
   }

   public void onUpdate() {
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      this.posX = this.posX + this.motionX;
      this.posY = this.posY + this.motionY;
      this.posZ = this.posZ + this.motionZ;
      Vector3 start = new Vector3(this.prevPosX, this.prevPosY, this.prevPosZ);
      Vector3 end = new Vector3(this.posX, this.posY, this.posZ);
      World world = this.getEntityWorld();
      RayTraceResult hit = world.rayTraceBlocks(start.toVec3(), end.toVec3(), true);
      if (hit != null) {
         end.set(hit.hitVec);
         this.posX = hit.hitVec.x;
         this.posY = hit.hitVec.y;
         this.posZ = hit.hitVec.z;
      }

      List<Entity> entitiesToCheck = world.getEntitiesWithinAABBExcludingEntity(
         this,
         new AxisAlignedBB(this.prevPosX, this.prevPosY, this.prevPosZ, this.posX, this.posY, this.posZ)
            .grow(this.influenceSize)
      );
      List<RayTraceResult> entitiesInfluences = new ArrayList<>();
      double minDistanceSq = start.distanceSquared(end);

      for (Entity entity : entitiesToCheck) {
         if (entity != this.owner && entity.canBeCollidedWith()) {
            RayTraceResult entityInfluence = entity.getEntityBoundingBox().grow(this.influenceSize).calculateIntercept(start.toVec3(), end.toVec3());
            if (entityInfluence != null) {
               entitiesInfluences.add(entityInfluence);
               RayTraceResult entityHit = entity.getEntityBoundingBox().grow(this.coreSize).calculateIntercept(start.toVec3(), end.toVec3());
               if (entityHit != null) {
                  double distanceSq = start.distanceSquared(entityHit.hitVec);
                  if (distanceSq < minDistanceSq) {
                     hit = entityHit;
                     minDistanceSq = distanceSq;
                  }
               }
            }
         }
      }

      double maxInfluenceDistance = Math.sqrt(minDistanceSq) + this.influenceSize;

      for (RayTraceResult entityInfluence : entitiesInfluences) {
         if (start.distance(entityInfluence.hitVec) <= maxInfluenceDistance) {
            this.onInfluence(entityInfluence);
         }
      }

      if (this.radialTestVectors != null) {
         Vector3 vForward = end.copy().sub(start);
         double len = vForward.length();
         vForward.scale(1.0 / len);
         Vector3 origin = new Vector3(start);
         Vector3 tmp = new Vector3();

         for (int d = 0; d < len; d++) {
            for (Vector3 radialTestVector : this.radialTestVectors) {
               origin.copy(tmp).add(radialTestVector);
               RayTraceResult influence = world.rayTraceBlocks(origin.toVec3(), tmp.toVec3(), true);
               if (influence != null) {
                  this.onInfluence(influence);
               }
            }

            origin.add(vForward);
         }
      }

      if (hit != null) {
         this.onImpact(hit);
         this.setDead();
      } else {
         this.lifeTime--;
         if (this.lifeTime <= 0) {
            this.setDead();
         }
      }
   }

   protected void onImpact(RayTraceResult hit) {
      if (IC2.platform.isSimulating()) {
         System.out.println("hit " + hit.typeOfHit + " " + hit.hitVec + " sim=" + IC2.platform.isSimulating());
         if (hit.typeOfHit == Type.BLOCK && IC2.platform.isSimulating()) {
         }

         ExplosionIC2 explosion = new ExplosionIC2(
            this.getEntityWorld(),
            this.owner,
            hit.hitVec.x,
            hit.hitVec.y,
            hit.hitVec.z,
            18.0F,
            0.95F,
            ExplosionIC2.Type.Heat
         );
         explosion.doExplosion();
      }
   }

   protected void onInfluence(RayTraceResult hit) {
      if (IC2.platform.isSimulating()) {
         System.out.println("influenced " + hit.typeOfHit + " " + hit.hitVec + " sim=" + IC2.platform.isSimulating());
         if (hit.typeOfHit == Type.BLOCK && IC2.platform.isSimulating()) {
            World world = this.getEntityWorld();
            IBlockState state = world.getBlockState(hit.getBlockPos());
            Block block = state.getBlock();
            if (block != Blocks.WATER && block != Blocks.FLOWING_WATER) {
               List<ItemStack> drops = StackUtil.getDrops(world, hit.getBlockPos(), state, null, 0, true);
               if (drops.size() == 1 && StackUtil.getSize(drops.get(0)) == 1) {
                  ItemStack existing = drops.get(0);
                  ItemStack smelted = FurnaceRecipes.instance().getSmeltingResult(existing);
                  if (smelted != null && smelted.getItem() instanceof ItemBlock) {
                     world.setBlockState(hit.getBlockPos(), ((ItemBlock)smelted.getItem()).getBlock().getDefaultState());
                  } else if (block.isFlammable(world, hit.getBlockPos(), hit.sideHit)) {
                     world.setBlockState(hit.getBlockPos().offset(hit.sideHit.getOpposite()), Blocks.FIRE.getDefaultState());
                  }
               }
            } else {
               world.setBlockToAir(hit.getBlockPos());
            }
         }
      }
   }
}
