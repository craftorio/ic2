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
    method_tickLerp = getMethod("tickLerp", "func_184447_s", new Class[0]);
    field_paddlePositions = getField("paddlePositions", "field_184470_f");
    field_previousStatus = getField("previousStatus", "field_184471_aG");
    field_status = getField("status", "field_184469_aF");
    field_outOfControlTicks = getField("outOfControlTicks", "field_184474_h");
    field_momentum = getField("momentum", "field_184472_g");
    field_lastYd = getField("lastYd", "field_184473_aH");
    field_waterLevel = getField("waterLevel", "field_184465_aD");
    field_boatGlide = getField("boatGlide", "field_184467_aE");
    field_deltaRotation = getField("deltaRotation", "field_184475_as");
    field_rightInputDown = getField("rightInputDown", "field_184459_aA");
    field_leftInputDown = getField("leftInputDown", "field_184480_az");
    field_forwardInputDown = getField("forwardInputDown", "field_184461_aB");
    field_backInputDown = getField("backInputDown", "field_184463_aC");
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
        func_184226_ay(); 
      if (func_70268_h() > 0)
        func_70265_b(func_70268_h() - 1); 
      if (func_70271_g() > 0.0F)
        func_70266_a(func_70271_g() - 1.0F); 
      this.prevPosX = this.posX;
      this.prevPosY = this.posY;
      this.prevPosZ = this.posZ;
      doEntityUpdate(world);
      method_tickLerp.invoke(this, new Object[0]);
      if (func_184186_bw()) {
        if (func_184188_bt().isEmpty() || !(func_184188_bt().get(0) instanceof net.minecraft.entity.player.EntityPlayer))
          func_184445_a(false, false); 
        updateMotion();
        if (world.isRemote) {
          controlBoat();
          world.func_184135_a((Packet)new CPacketSteerBoat(func_184457_a(0), func_184457_a(1)));
        } 
        move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
      } else {
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
      } 
      for (int i = 0; i <= 1; i++) {
        if (func_184457_a(i)) {
          double paddlePosition = Array.getFloat(field_paddlePositions.get(this), i);
          if (!func_174814_R() && paddlePosition % 6.283185307179586D <= 0.7853981633974483D && (paddlePosition + 0.4D) % 6.283185307179586D >= 0.7853981633974483D) {
            SoundEvent soundevent = func_193047_k();
            if (soundevent != null) {
              Vec3d look = func_70676_i(1.0F);
              world.func_184148_a(null, this.posX + ((i == 1) ? -look.field_72449_c : look.field_72449_c), this.posY, this.posZ + ((i == 1) ? look.field_72450_a : -look.field_72450_a), soundevent, 
                  func_184176_by(), 1.0F, 0.8F + 0.4F * this.field_70146_Z.nextFloat());
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
    func_145775_I();
    List<Entity> list = world.func_175674_a((Entity)this, func_174813_aQ().func_72314_b(0.2D, -0.01D, 0.2D), EntitySelectors.func_188442_a((Entity)this));
    if (!list.isEmpty()) {
      boolean flag = (!world.isRemote && !(func_184179_bs() instanceof net.minecraft.entity.player.EntityPlayer));
      for (Entity entity : list) {
        if (!entity.func_184196_w((Entity)this)) {
          if (flag && func_184188_bt().size() < 2 && !entity.func_184218_aH() && entity.field_70130_N < this.field_70130_N && entity instanceof net.minecraft.entity.EntityLivingBase && !(entity instanceof net.minecraft.entity.passive.EntityWaterMob) && !(entity instanceof net.minecraft.entity.player.EntityPlayer)) {
            entity.func_184220_m((Entity)this);
            continue;
          } 
          func_70108_f(entity);
        } 
      } 
    } 
  }
  
  private void doEntityUpdate(World world) {
    if (!world.isRemote)
      func_70052_a(6, func_184202_aL()); 
    func_70030_z();
  }
  
  private void updateMotion() {
    double generalHeightChangingValue = func_189652_ae() ? 0.0D : -0.04D;
    double heightChange = 0.0D;
    float momentum = 0.05F;
    try {
      EntityBoat.Status status = (EntityBoat.Status)field_status.get(this);
      if (field_previousStatus.get(this) == EntityBoat.Status.IN_AIR && status != EntityBoat.Status.IN_AIR && status != EntityBoat.Status.ON_LAND) {
        field_waterLevel.setDouble(this, (func_174813_aQ()).field_72338_b + this.field_70131_O);
        setPosition(this.posX, (func_184451_k() - this.field_70131_O) + 0.101D, this.posZ);
        this.motionY = 0.0D;
        field_lastYd.setDouble(this, 0.0D);
        field_status.set(this, EntityBoat.Status.IN_WATER);
      } else {
        switch (status) {
          case IN_AIR:
            momentum = 0.9F;
            break;
          case IN_WATER:
            heightChange = (field_waterLevel.getDouble(this) - (func_174813_aQ()).field_72338_b) / this.field_70131_O;
            momentum = 0.9F;
            break;
          case ON_LAND:
            momentum = field_boatGlide.getFloat(this);
            if (func_184179_bs() instanceof net.minecraft.entity.player.EntityPlayer)
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
  
  public float func_184451_k() {
    AxisAlignedBB boundingBox = func_174813_aQ();
    int minX = (int)Math.floor(boundingBox.field_72340_a);
    int maxX = (int)Math.ceil(boundingBox.field_72336_d);
    int minZ = (int)Math.floor(boundingBox.field_72339_c);
    int maxZ = (int)Math.ceil(boundingBox.field_72334_f);
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.func_185346_s();
    try {
      World world = getEntityWorld();
      int maxY = (int)Math.ceil(boundingBox.field_72337_e - field_lastYd.getDouble(this));
      for (int y = (int)Math.floor(boundingBox.field_72337_e); y < maxY; y++) {
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
      blockPosPool.func_185344_t();
    } 
  }
  
  private EntityBoat.Status getBoatStatus() {
    EntityBoat.Status isUnderWater = getUnderwaterStatus();
    try {
      if (isUnderWater != null) {
        field_waterLevel.setDouble(this, (func_174813_aQ()).field_72337_e);
        return isUnderWater;
      } 
      if (checkInWater())
        return EntityBoat.Status.IN_WATER; 
      float glideSpeed = func_184441_l();
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
    AxisAlignedBB boundingBox = func_174813_aQ();
    boolean isInWater = false;
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.func_185346_s();
    try {
      double waterLevel = Double.MIN_VALUE;
      for (int x = (int)Math.floor(boundingBox.field_72340_a); x < Math.ceil(boundingBox.field_72336_d); x++) {
        for (int y = (int)Math.floor(boundingBox.field_72338_b); y < Math.ceil(boundingBox.field_72338_b + 0.001D); y++) {
          for (int z = (int)Math.floor(boundingBox.field_72339_c); z < Math.ceil(boundingBox.field_72334_f); z++) {
            blockPosPool.setPos(x, y, z);
            IBlockState block = world.getBlockState((BlockPos)blockPosPool);
            if (isWater(block)) {
              float waterHeight = getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool);
              waterLevel = Math.max(waterHeight, waterLevel);
              i = isInWater | ((boundingBox.field_72338_b < waterHeight) ? 1 : 0);
            } 
          } 
        } 
      } 
      field_waterLevel.setDouble(this, waterLevel);
    } catch (Exception e) {
      throw new RuntimeException("Error reflecting boat in checkInWater", e);
    } finally {
      blockPosPool.func_185344_t();
    } 
    return i;
  }
  
  @Nullable
  private EntityBoat.Status getUnderwaterStatus() {
    World world = getEntityWorld();
    AxisAlignedBB boundingBox = func_174813_aQ();
    double boatTop = boundingBox.field_72337_e + 0.001D;
    BlockPos.PooledMutableBlockPos blockPosPool = BlockPos.PooledMutableBlockPos.func_185346_s();
    try {
      for (int x = (int)Math.floor(boundingBox.field_72340_a); x < Math.ceil(boundingBox.field_72336_d); x++) {
        for (int y = (int)Math.floor(boundingBox.field_72337_e); y < Math.ceil(boatTop); y++) {
          for (int z = (int)Math.floor(boundingBox.field_72339_c); z < Math.ceil(boundingBox.field_72334_f); z++) {
            blockPosPool.setPos(x, y, z);
            IBlockState block = world.getBlockState((BlockPos)blockPosPool);
            if (isWater(block) && boatTop < getLiquidHeight(block, (IBlockAccess)world, (BlockPos)blockPosPool))
              return (((Integer)block.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue() != 0) ? EntityBoat.Status.UNDER_FLOWING_WATER : EntityBoat.Status.UNDER_WATER; 
          } 
        } 
      } 
    } finally {
      blockPosPool.func_185344_t();
    } 
    return null;
  }
  
  public static float getLiquidHeight(IBlockState block, IBlockAccess world, BlockPos pos) {
    return pos.getY() + getBlockLiquidHeight(block, world, pos);
  }
  
  public static float getBlockLiquidHeight(IBlockState block, IBlockAccess world, BlockPos pos) {
    int liquidHeight = ((Integer)block.func_177229_b((IProperty)BlockLiquid.field_176367_b)).intValue();
    return ((liquidHeight & 0x7) == 0 && world.getBlockState(pos.up()).getMaterial() == block.getMaterial()) ? 1.0F : (1.0F - BlockLiquid.func_149801_b(liquidHeight));
  }
  
  private void controlBoat() {
    if (func_184207_aI()) {
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
        func_184445_a(((right && !left) || forward), ((left && !right) || forward));
      } catch (Exception e) {
        throw new RuntimeException("Error reflecting boat in controlBoat", e);
      } 
    } 
  }
  
  protected void func_184231_a(double y, boolean onGround, IBlockState state, BlockPos pos) {
    boolean expectDeath = (this.field_70143_R > 3.0F && !this.field_70128_L);
    super.func_184231_a(y, onGround, state, pos);
    if (expectDeath && this.field_70128_L && getEntityWorld().func_82736_K().func_82766_b("doEntityDrops"))
      super.func_70099_a(getBrokenItem(), 0.0F); 
  }
  
  public EntityItem func_70099_a(ItemStack stack, float offsetY) {
    if (stack.getItem() == Items.field_151124_az)
      return super.func_70099_a(getItem(), offsetY); 
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
