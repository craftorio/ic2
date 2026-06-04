package ic2.core.item;

import ic2.core.util.ReflectionUtil;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.CPacketSteerBoat;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
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
    method_tickLerp = getMethod("tickLerp", "tickLerp", new Class[0]);
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
    return ReflectionUtil.getField(EntityBoat.class, new String[] { srgName, deobfName });
  }
  
  private static Method getMethod(String deobfName, String srgName, Class<?>... parameterTypes) {
    return ReflectionUtil.getMethod(EntityBoat.class, new String[] { srgName, deobfName }, parameterTypes);
  }
  
  public EntityIC2Boat(World world) {
    super(world);
  }
  
  public void onUpdate() {
    World world = getEntityWorld();
    try {
      field_previousStatus.set(this, field_status.get(this));
      EntityBoat.Status status = getBoatStatus();
      field_status.set(this, status);
      if (status != EntityBoat.Status.UNDER_WATER && status != EntityBoat.Status.UNDER_FLOWING_WATER) {
        field_outOfControlTicks.setFloat(this, 0.0F);
      } else {
        field_outOfControlTicks.setFloat(this, field_outOfControlTicks.getFloat(this) + 1.0F);
      } 
      if (!world.isRemote && field_outOfControlTicks.getFloat(this) >= 60.0F)
        removePassengers(); 
      if (getTimeSinceHit() > 0)
        setTimeSinceHit(getTimeSinceHit() - 1); 
      if (getDamageTaken() > 0.0F)
        setDamageTaken(getDamageTaken() - 1.0F); 
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      doEntityUpdate(world);
      method_tickLerp.invoke(this, new Object[0]);
      if (canPassengerSteer()) {
        if (getPassengers().isEmpty() || !(getPassengers().get(0) instanceof net.minecraft.entity.player.EntityPlayer))
          setPaddleState(false, false); 
        updateMotion();
        if (world.isRemote) {
          controlBoat();
          world.sendPacketToServer((Packet)new CPacketSteerBoat(getPaddleState(0), getPaddleState(1)));
        } 
        move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
      } else {
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
      } 
      for (int i = 0; i <= 1; i++) {
        if (getPaddleState(i)) {
          double paddlePosition = Array.getFloat(field_paddlePositions.get(this), i);
          if (!isSilent() && paddlePosition % 6.283185307179586D <= 0.7853981633974483D && (paddlePosition + 0.4D) % 6.283185307179586D >= 0.7853981633974483D) {
            SoundEvent soundevent = getPaddleSound();
            if (soundevent != null) {
              Vec3d look = getLook(1.0F);
              world.playSound(null, this.posX + ((i == 1) ? -look.z : look.z), this.posY, this.posZ + ((i == 1) ? look.x : -look.x), soundevent, 
                  getSoundCategory(), 1.0F, 0.8F + 0.4F * this.rand.nextFloat());
            } 
          } 
          Array.setFloat(field_paddlePositions.get(this), i, (float)(paddlePosition + 0.04D));
        } else {
          Array.setFloat(field_paddlePositions.get(this), i, 0.0F);
        } 
      } 
    } catch (Exception e) {
      throw new RuntimeException("Error reflecting boat in update", e);
    } 
    doBlockCollisions();
    List<Entity> list = world.getEntitiesInAABBexcluding((Entity)this, getEntityBoundingBox().grow(0.2D, -0.01D, 0.2D), EntitySelectors.getTeamCollisionPredicate((Entity)this));
    if (!list.isEmpty()) {
      boolean flag = (!world.isRemote && !(getControllingPassenger() instanceof net.minecraft.entity.player.EntityPlayer));
      for (Entity entity : list) {
        if (!entity.isPassenger((Entity)this)) {
          if (flag && getPassengers().size() < 2 && !entity.isRiding() && entity.width < this.width && entity instanceof net.minecraft.entity.EntityLivingBase && !(entity instanceof net.minecraft.entity.passive.EntityWaterMob) && !(entity instanceof net.minecraft.entity.player.EntityPlayer)) {
            entity.startRiding((Entity)this);
            continue;
          } 
          applyEntityCollision(entity);
        } 
      } 
    } 
  }
  
  private void doEntityUpdate(World world) {
    if (!world.isRemote)
      setFlag(6, isGlowing()); 
    onEntityUpdate();
  }
  
  private void updateMotion() {
    double generalHeightChangingValue = hasNoGravity() ? 0.0D : -0.04D;
    double heightChange = 0.0D;
    float momentum = 0.05F;
    try {
      EntityBoat.Status status = (EntityBoat.Status)field_status.get(this);
      if (field_previousStatus.get(this) == EntityBoat.Status.IN_AIR && status != EntityBoat.Status.IN_AIR && status != EntityBoat.Status.ON_LAND) {
        field_waterLevel.setDouble(this, (getEntityBoundingBox()).minY + this.height);
        setPosition(this.posX, (getWaterLevelAbove() - this.height) + 0.101D, this.posZ);
        this.motionY = 0.0D;
        field_lastYd.setDouble(this, 0.0D);
        field_status.set(this, EntityBoat.Status.IN_WATER);
      } else {
        switch (status) {
          case IN_AIR:
            momentum = 0.9F;
            break;
          case IN_WATER:
            heightChange = (field_waterLevel.getDouble(this) - (getEntityBoundingBox()).minY) / this.height;
            momentum = 0.9F;
            break;
          case ON_LAND:
            momentum = field_boatGlide.getFloat(this);
            if (getControllingPassenger() instanceof net.minecraft.entity.player.EntityPlayer)
              field_boatGlide.setFloat(this, momentum / 2.0F); 
            break;
          case UNDER_FLOWING_WATER:
            generalHeightChangingValue = -7.0E-4D;
            momentum = 0.9F;
            break;
          case UNDER_WATER:
            heightChange = 0.01D;
            momentum = 0.45F;
            break;
        } 
        this.motionX *= momentum;
        this.motionZ *= momentum;
        field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) * momentum);
        this.motionY += generalHeightChangingValue;
        if (heightChange > 0.0D) {
          this.motionY += heightChange * 0.061538461538461535D;
          this.motionY *= 0.75D;
        } 
      } 
      field_momentum.setFloat(this, momentum);
    } catch (Exception e) {
      throw new RuntimeException("Error reflecting boat in updateMotion", e);
    } 
  }
  
  public float getWaterLevelAbove() {
    AxisAlignedBB boundingBox = getEntityBoundingBox();
    int minX = (int)Math.floor(boundingBox.minX);
    int maxX = (int)Math.ceil(boundingBox.maxX);
    int minZ = (int)Math.floor(boundingBox.minZ);
    int maxZ = (int)Math.ceil(boundingBox.maxZ);
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
    try {
      World world = getEntityWorld();
      int maxY = (int)Math.ceil(boundingBox.maxY - field_lastYd.getDouble(this));
      for (int y = (int)Math.floor(boundingBox.maxY); y < maxY; y++) {
        float waterHeight = 0.0F;
        int x = minX;
        label29: while (true) {
          if (x >= maxX) {
            if (waterHeight < 1.0F)
              return blockPosPool.getY() + waterHeight; 
            break;
          } 
          for (int z = minZ; z < maxZ; z++) {
            blockPosPool.setPos(x, y, z);
            IBlockState block = world.getBlockState((BlockPos)blockPosPool);
            if (isWater(block))
              waterHeight = Math.max(waterHeight, getBlockLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool)); 
            if (waterHeight >= 1.0F)
              break label29; 
          } 
          x++;
        } 
      } 
      return (maxY + 1);
    } catch (Exception e) {
      throw new RuntimeException("Error reflecting boat in getWaterLevelAbove", e);
    } finally {
      blockPosPool.release();
    } 
  }
  
  private EntityBoat.Status getBoatStatus() {
    EntityBoat.Status isUnderWater = getUnderwaterStatus();
    try {
      if (isUnderWater != null) {
        field_waterLevel.setDouble(this, (getEntityBoundingBox()).maxY);
        return isUnderWater;
      } 
      if (checkInWater())
        return EntityBoat.Status.IN_WATER; 
      float glideSpeed = getBoatGlide();
      if (glideSpeed > 0.0F) {
        field_boatGlide.setFloat(this, glideSpeed);
        return EntityBoat.Status.ON_LAND;
      } 
      return EntityBoat.Status.IN_AIR;
    } catch (Exception e) {
      throw new RuntimeException("Error reflecting boat in getBoatStatus", e);
    } 
  }
  
  private boolean checkInWater() {
    int i;
    World world = getEntityWorld();
    AxisAlignedBB boundingBox = getEntityBoundingBox();
    boolean isInWater = false;
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
    try {
      double waterLevel = Double.MIN_VALUE;
      for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); x++) {
        for (int y = (int)Math.floor(boundingBox.minY); y < Math.ceil(boundingBox.minY + 0.001D); y++) {
          for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); z++) {
            blockPosPool.setPos(x, y, z);
            IBlockState block = world.getBlockState((BlockPos)blockPosPool);
            if (isWater(block)) {
              float waterHeight = getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool);
              waterLevel = Math.max(waterHeight, waterLevel);
              i = isInWater | ((boundingBox.minY < waterHeight) ? 1 : 0);
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
    return i;
  }
  
  @Nullable
  private EntityBoat.Status getUnderwaterStatus() {
    World world = getEntityWorld();
    AxisAlignedBB boundingBox = getEntityBoundingBox();
    double boatTop = boundingBox.maxY + 0.001D;
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.retain();
    try {
      for (int x = (int)Math.floor(boundingBox.minX); x < Math.ceil(boundingBox.maxX); x++) {
        for (int y = (int)Math.floor(boundingBox.maxY); y < Math.ceil(boatTop); y++) {
          for (int z = (int)Math.floor(boundingBox.minZ); z < Math.ceil(boundingBox.maxZ); z++) {
            blockPosPool.setPos(x, y, z);
            IBlockState block = world.getBlockState((BlockPos)blockPosPool);
            if (isWater(block) && boatTop < getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool))
              return (((Integer)block.getValue((IProperty)BlockLiquid.LEVEL)).intValue() != 0) ? EntityBoat.Status.UNDER_FLOWING_WATER : EntityBoat.Status.UNDER_WATER; 
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
    int liquidHeight = ((Integer)block.getValue((IProperty)BlockLiquid.LEVEL)).intValue();
    return ((liquidHeight & 0x7) == 0 && world.getBlockState(pos.up()).getMaterial() == block.getMaterial()) ? 1.0F : (1.0F - BlockLiquid.getLiquidHeightPercent(liquidHeight));
  }
  
  private void controlBoat() {
    if (isBeingRidden()) {
      float speed = 0.0F;
      try {
        boolean left = field_leftInputDown.getBoolean(this);
        boolean right = field_rightInputDown.getBoolean(this);
        boolean forward = field_forwardInputDown.getBoolean(this);
        boolean backward = field_backInputDown.getBoolean(this);
        if (left)
          field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) - 1.0F); 
        if (right)
          field_deltaRotation.setFloat(this, field_deltaRotation.getFloat(this) + 1.0F); 
        if (right != left && !forward && !backward)
          speed += 0.005F; 
        this.rotationYaw += field_deltaRotation.getFloat(this);
        if (forward)
          speed += 0.04F; 
        if (backward)
          speed -= 0.005F; 
        this.motionX += (MathHelper.sin(-this.rotationYaw * 3.1415927F / 180.0F) * speed) * getAccelerationFactor();
        this.motionZ += (MathHelper.cos(this.rotationYaw * 3.1415927F / 180.0F) * speed) * getAccelerationFactor();
        setPaddleState(((right && !left) || forward), ((left && !right) || forward));
      } catch (Exception e) {
        throw new RuntimeException("Error reflecting boat in controlBoat", e);
      } 
    } 
  }
  
  protected void updateFallState(double y, boolean onGround, IBlockState state, BlockPos pos) {
    boolean expectDeath = (this.fallDistance > 3.0F && !this.isDead);
    super.updateFallState(y, onGround, state, pos);
    if (expectDeath && this.isDead && getEntityWorld().getGameRules().getBoolean("doEntityDrops"))
      super.entityDropItem(getBrokenItem(), 0.0F); 
  }
  
  public EntityItem entityDropItem(ItemStack stack, float offsetY) {
    if (stack.getItem() == Items.BOAT)
      return super.entityDropItem(getItem(), offsetY); 
    return null;
  }
  
  public ItemStack getPickedResult(RayTraceResult target) {
    return getItem();
  }
  
  protected abstract ItemStack getItem();
  
  protected ItemStack getBrokenItem() {
    return getItem();
  }
  
  public abstract String getTexture();
  
  protected double getAccelerationFactor() {
    return 1.0D;
  }
  
  protected double getTopSpeed() {
    return 0.35D;
  }
  
  protected boolean isWater(IBlockState block) {
    return (block.getMaterial() == Material.WATER);
  }
}
