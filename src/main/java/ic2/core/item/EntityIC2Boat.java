package ic2.core.item;

import ic2.core.util.ReflectionUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityBoat.Status;
import net.minecraft.entity.passive.EntityWaterMob;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.BlockPos.PooledMutableBlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class EntityIC2Boat extends EntityBoat {
   private static Method method_tickLerp;
   private static Field field_paddlePositions;
   private static Field field_previousStatus;
   private static Field field_status;
   private static Field field_outOfControlTicks;
   private static Field field_momentum;
   private static Field field_lastYd;
   private static Field field_waterLevel;
   private static Field field_boatGlide;
   private static Field field_deltaRotation;
   private static Field field_rightInputDown;
   private static Field field_leftInputDown;
   private static Field field_forwardInputDown;
   private static Field field_backInputDown;

   public static void init() {
      method_tickLerp = getMethod("tickLerp", "tickLerp");
      field_paddlePositions = getField("paddlePositions", "paddlePositions");
      field_previousStatus = getField("previousStatus", "previousStatus");
      field_status = getField("status", "status");
      field_outOfControlTicks = getField("outOfControlTicks", "outOfControlTicks");
      field_momentum = getField("momentum", "momentum");
      field_lastYd = getField("lastYd", "lastYd");
      field_waterLevel = getField("waterLevel", "waterLevel");
      field_boatGlide = getField("boatGlide", "boatGlide");
      field_deltaRotation = getField("deltaRotation", "deltaRotation");
      field_rightInputDown = getField("rightInputDown", "rightInputDown");
      field_leftInputDown = getField("leftInputDown", "leftInputDown");
      field_forwardInputDown = getField("forwardInputDown", "forwardInputDown");
      field_backInputDown = getField("backInputDown", "backInputDown");
   }

   private static Field getField(String deobfName, String srgName) {
      return ReflectionUtil.getField(EntityBoat.class, srgName, deobfName);
   }

   private static Method getMethod(String deobfName, String srgName, Class<?>... parameterTypes) {
      return ReflectionUtil.getMethod(EntityBoat.class, new String[]{srgName, deobfName}, parameterTypes);
   }

   public EntityIC2Boat(World world) {
      super(world);
   }

   public void onUpdate() {
      World world = this.getEntityWorld();

      try {
         field_previousStatus.set(this, field_status.get(this));
         Status status = this.getBoatStatus();
         field_status.set(this, status);
         if (status != Status.UNDER_WATER && status != Status.UNDER_FLOWING_WATER) {
            field_outOfControlTicks.setFloat(this, 0.0F);
         } else {
            field_outOfControlTicks.setFloat(this, field_outOfControlTicks.getFloat(this) + 1.0F);
         }

         if (!world.isRemote && field_outOfControlTicks.getFloat(this) >= 60.0F) {
            this.removePassengers();
         }

         if (this.getTimeSinceHit() > 0) {
            this.setTimeSinceHit(this.getTimeSinceHit() - 1);
         }

         if (this.getDamageTaken() > 0.0F) {
            this.setDamageTaken(this.getDamageTaken() - 1.0F);
         }

         this.prevPosX = this.posX;
         this.prevPosY = this.posY;
         this.prevPosZ = this.posZ;
         this.doEntityUpdate(world);
         method_tickLerp.invoke(this);
         if (!this.canPassengerSteer()) {
            this.motionX = 0.0;
            this.motionY = 0.0;
            this.motionZ = 0.0;
         } else {
            if (this.getPassengers().isEmpty() || !(this.getPassengers().get(0) instanceof EntityPlayer)) {
               this.setPaddleState(false, false);
            }

            this.updateMotion();
            if (world.isRemote) {
               this.controlBoat();
               world.sendPacketToServer(new CPacketSteerBoat(this.getPaddleState(0), this.getPaddleState(1)));
            }

            this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
         }

         for (int i = 0; i <= 1; i++) {
            if (this.getPaddleState(i)) {
               double paddlePosition = Array.getFloat(field_paddlePositions.get(this), i);
               if (!this.isSilent() && paddlePosition % (Math.PI * 2) <= Math.PI / 4 && (paddlePosition + 0.4) % (Math.PI * 2) >= Math.PI / 4) {
                  SoundEvent soundevent = this.getPaddleSound();
                  if (soundevent != null) {
                     Vec3d look = this.getLook(1.0F);
                     world.playSound(
                        null,
                        this.posX + (i == 1 ? -look.z : look.z),
                        this.posY,
                        this.posZ + (i == 1 ? look.x : -look.x),
                        soundevent,
                        this.getSoundCategory(),
                        1.0F,
                        0.8F + 0.4F * this.rand.nextFloat()
                     );
                  }
               }

               Array.setFloat(field_paddlePositions.get(this), i, (float)(paddlePosition + 0.04));
            } else {
               Array.setFloat(field_paddlePositions.get(this), i, 0.0F);
            }
         }
      } catch (Exception e) {
         throw new RuntimeException("Error reflecting boat in update", e);
      }

      this.doBlockCollisions();
      List<Entity> list = world.getEntitiesInAABBexcluding(this, this.getEntityBoundingBox().grow(0.2, -0.01, 0.2), EntitySelectors.getTeamCollisionPredicate(this));
      if (!list.isEmpty()) {
         boolean flag = !world.isRemote && !(this.getControllingPassenger() instanceof EntityPlayer);

         for (Entity entity : list) {
            if (!entity.isPassenger(this)) {
               if (flag
                  && this.getPassengers().size() < 2
                  && !entity.isRiding()
                  && entity.width < this.width
                  && entity instanceof EntityLivingBase
                  && !(entity instanceof EntityWaterMob)
                  && !(entity instanceof EntityPlayer)) {
                  entity.startRiding(this);
               } else {
                  this.applyEntityCollision(entity);
               }
            }
         }
      }
   }

   private void doEntityUpdate(World world) {
      if (!world.isRemote) {
         this.setFlag(6, this.isGlowing());
      }

      this.onEntityUpdate();
   }

   private void updateMotion() {
      double generalHeightChangingValue = this.hasNoGravity() ? 0.0 : -0.04;
      double heightChange = 0.0;
      float momentum = 0.05F;

      try {
         Status status = (Status)field_status.get(this);
         if (field_previousStatus.get(this) == Status.IN_AIR && status != Status.IN_AIR && status != Status.ON_LAND) {
            field_waterLevel.setDouble(this, this.getEntityBoundingBox().minY + this.height);
            this.setPosition(this.posX, this.getWaterLevelAbove() - this.height + 0.101, this.posZ);
            this.motionY = 0.0;
            field_lastYd.setDouble(this, 0.0);
            field_status.set(this, Status.IN_WATER);
         } else {
            switch (status) {
               case IN_AIR:
                  momentum = 0.9F;
                  break;
               case IN_WATER:
                  heightChange = (field_waterLevel.getDouble(this) - this.getEntityBoundingBox().minY) / this.height;
                  momentum = 0.9F;
                  break;
               case ON_LAND:
                  momentum = field_boatGlide.getFloat(this);
                  if (this.getControllingPassenger() instanceof EntityPlayer) {
                     field_boatGlide.setFloat(this, momentum / 2.0F);
                  }
                  break;
               case UNDER_FLOWING_WATER:
                  generalHeightChangingValue = -7.0E-4;
                  momentum = 0.9F;
                  break;
               case UNDER_WATER:
                  heightChange = 0.01;
                  momentum = 0.45F;
            }

            this.motionX *= momentum;
            this.motionZ *= momentum;
            field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) * momentum);
            this.motionY += generalHeightChangingValue;
            if (heightChange > 0.0) {
               this.motionY += heightChange * 0.061538461538461535;
               this.motionY *= 0.75;
            }
         }

         field_momentum.setFloat(this, momentum);
      } catch (Exception e) {
         throw new RuntimeException("Error reflecting boat in updateMotion", e);
      }
   }

   public float getWaterLevelAbove() {
      AxisAlignedBB boundingBox = this.getEntityBoundingBox();
      int minX = (int)Math.floor(boundingBox.minX);
      int maxX = (int)Math.ceil(boundingBox.maxX);
      int minZ = (int)Math.floor(boundingBox.minZ);
      int maxZ = (int)Math.ceil(boundingBox.maxZ);
      PooledMutableBlockPos blockPosPool = PooledMutableBlockPos.retain();

      try {
         World world = this.getEntityWorld();
         int maxY = (int)Math.ceil(boundingBox.maxY - field_lastYd.getDouble(this));

         label90:
         for (int y = (int)Math.floor(boundingBox.maxY); y < maxY; y++) {
            float waterHeight = 0.0F;

            for (int x = minX; x < maxX; x++) {
               for (int z = minZ; z < maxZ; z++) {
                  blockPosPool.setPos(x, y, z);
                  IBlockState block = world.getBlockState(blockPosPool);
                  if (this.isWater(block)) {
                     waterHeight = Math.max(waterHeight, getBlockLiquidHeight(block, world, blockPosPool));
                  }

                  if (waterHeight >= 1.0F) {
                     continue label90;
                  }
               }
            }

            if (waterHeight < 1.0F) {
               return blockPosPool.getY() + waterHeight;
            }
         }

         return maxY + 1;
      } catch (Exception e) {
         throw new RuntimeException("Error reflecting boat in getWaterLevelAbove", e);
      } finally {
         blockPosPool.release();
      }
   }

   private Status getBoatStatus() {
      Status isUnderWater = this.getUnderwaterStatus();

      try {
         if (isUnderWater != null) {
            field_waterLevel.setDouble(this, this.getEntityBoundingBox().maxY);
            return isUnderWater;
         } else if (this.checkInWater()) {
            return Status.IN_WATER;
         } else {
            float glideSpeed = this.getBoatGlide();
            if (glideSpeed > 0.0F) {
               field_boatGlide.setFloat(this, glideSpeed);
               return Status.ON_LAND;
            } else {
               return Status.IN_AIR;
            }
         }
      } catch (Exception e) {
         throw new RuntimeException("Error reflecting boat in getBoatStatus", e);
      }
   }

   private boolean checkInWater() {
      World world = this.getEntityWorld();
      AxisAlignedBB boundingBox = this.getEntityBoundingBox();
      boolean isInWater = false;
      PooledMutableBlockPos blockPosPool = PooledMutableBlockPos.retain();

      try {
         double waterLevel = Double.MIN_VALUE;

         for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); x++) {
            for (int y = (int)Math.floor(boundingBox.minY); y < Math.ceil(boundingBox.minY + 0.001); y++) {
               for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); z++) {
                  blockPosPool.setPos(x, y, z);
                  IBlockState block = world.getBlockState(blockPosPool);
                  if (this.isWater(block)) {
                     float waterHeight = getLiquidHeight(block, world, blockPosPool);
                     waterLevel = Math.max(waterHeight, waterLevel);
                     isInWater |= boundingBox.minY < waterHeight;
                  }
               }
            }
         }

         field_waterLevel.setDouble(this, waterLevel);
      } catch (Exception e) {
         throw new RuntimeException("Error reflecting boat in checkInWater", e);
      } finally {
         blockPosPool.release();
      }

      return isInWater;
   }

   @Nullable
   private Status getUnderwaterStatus() {
      World world = this.getEntityWorld();
      AxisAlignedBB boundingBox = this.getEntityBoundingBox();
      double boatTop = boundingBox.maxY + 0.001;
      PooledMutableBlockPos blockPosPool = PooledMutableBlockPos.retain();

      try {
         for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); x++) {
            for (int y = (int)Math.floor(boundingBox.maxY); y < Math.ceil(boatTop); y++) {
               for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); z++) {
                  blockPosPool.setPos(x, y, z);
                  IBlockState block = world.getBlockState(blockPosPool);
                  if (this.isWater(block) && boatTop < getLiquidHeight(block, world, blockPosPool)) {
                     return block.getValue(BlockLiquid.LEVEL) != 0 ? Status.UNDER_FLOWING_WATER : Status.UNDER_WATER;
                  }
               }
            }
         }
      } finally {
         blockPosPool.release();
      }

      return null;
   }

   public static float getLiquidHeight(IBlockState block, IBlockAccess world, BlockPos pos) {
      return pos.getY() + getBlockLiquidHeight(block, world, pos);
   }

   public static float getBlockLiquidHeight(IBlockState block, IBlockAccess world, BlockPos pos) {
      int liquidHeight = (Integer)block.getValue(BlockLiquid.LEVEL);
      return (liquidHeight & 7) == 0 && world.getBlockState(pos.up()).getMaterial() == block.getMaterial()
         ? 1.0F
         : 1.0F - BlockLiquid.getLiquidHeightPercent(liquidHeight);
   }

   private void controlBoat() {
      if (this.isBeingRidden()) {
         float speed = 0.0F;

         try {
            boolean left = field_leftInputDown.getBoolean(this);
            boolean right = field_rightInputDown.getBoolean(this);
            boolean forward = field_forwardInputDown.getBoolean(this);
            boolean backward = field_backInputDown.getBoolean(this);
            if (left) {
               field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) - 1.0F);
            }

            if (right) {
               field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) + 1.0F);
            }

            if (right != left && !forward && !backward) {
               speed += 0.005F;
            }

            this.rotationYaw = this.rotationYaw + field_deltaRotation.getFloat(this);
            if (forward) {
               speed += 0.04F;
            }

            if (backward) {
               speed -= 0.005F;
            }

            this.motionX = this.motionX
               + MathHelper.sin(-this.rotationYaw * (float) Math.PI / 180.0F) * speed * this.getAccelerationFactor();
            this.motionZ = this.motionZ
               + MathHelper.cos(this.rotationYaw * (float) Math.PI / 180.0F) * speed * this.getAccelerationFactor();
            this.setPaddleState(right && !left || forward, left && !right || forward);
         } catch (Exception e) {
            throw new RuntimeException("Error reflecting boat in controlBoat", e);
         }
      }
   }

   protected void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos) {
      boolean expectDeath = this.fallDistance > 3.0F && !this.isDead;
      super.updateFallState(y, onGround, state, pos);
      if (expectDeath && this.isDead && this.getEntityWorld().getGameRules().getBoolean("doEntityDrops")) {
         super.entityDropItem(this.getBrokenItem(), 0.0F);
      }
   }

   public EntityItem entityDropItem(ItemStack stack, float offsetY) {
      return stack.getItem() == Items.BOAT ? super.entityDropItem(this.getItem(), offsetY) : null;
   }

   public ItemStack getPickedResult(RayTraceResult target) {
      return this.getItem();
   }

   protected abstract ItemStack getItem();

   protected ItemStack getBrokenItem() {
      return this.getItem();
   }

   public abstract String getTexture();

   protected double getAccelerationFactor() {
      return 1.0;
   }

   protected double getTopSpeed() {
      return 0.35;
   }

   protected boolean isWater(IBlockState block) {
      return block.getMaterial() == Material.WATER;
   }
}
