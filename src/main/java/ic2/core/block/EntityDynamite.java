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
  
  public int fuse;
  
  private boolean inGround;
  
  public EntityLivingBase owner;
  
  private int ticksInGround;
  
  public EntityDynamite(World world, double x, double y, double z) {
    super(world);
    this.fuse = 100;
    this.inGround = false;
    setSize(0.5F, 0.5F);
    setPosition(x, y, z);
  }
  
  public EntityDynamite(World world) {
    this(world, 0.0D, 0.0D, 0.0D);
  }
  
  public EntityDynamite(World world, EntityLivingBase owner) {
    super(world);
    this.fuse = 100;
    this.inGround = false;
    this.owner = owner;
    setSize(0.5F, 0.5F);
    Vector3 eyePos = Util.getEyePosition((Entity)owner);
    func_70012_b(eyePos.x, eyePos.y, eyePos.z, owner.rotationYaw, owner.rotationPitch);
    this.posX -= Math.cos(Math.toRadians(this.rotationYaw)) * 0.16D;
    this.posY -= 0.1D;
    this.posZ -= Math.sin(Math.toRadians(this.rotationYaw)) * 0.16D;
    setPosition(this.posX, this.posY, this.posZ);
    this.motionX = -Math.sin(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
    this.motionZ = Math.cos(Math.toRadians(this.rotationYaw)) * Math.cos(Math.toRadians(this.rotationPitch));
    this.motionY = -Math.sin(Math.toRadians(this.rotationPitch));
    func_70186_c(this.motionX, this.motionY, this.motionZ, 1.0F, 1.0F);
  }
  
  protected void entityInit() {}
  
  public void func_70186_c(double x, double y, double z, float velocity, float inaccuracy) {
    double len = Math.sqrt(x * x + y * y + z * z);
    x /= len;
    y /= len;
    z /= len;
    x += this.field_70146_Z.nextGaussian() * 0.0075D * inaccuracy;
    y += this.field_70146_Z.nextGaussian() * 0.0075D * inaccuracy;
    z += this.field_70146_Z.nextGaussian() * 0.0075D * inaccuracy;
    x *= velocity;
    y *= velocity;
    z *= velocity;
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;
    double hLen = Math.sqrt(x * x + z * z);
    this.field_70126_B = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
    this.field_70127_C = this.rotationPitch = (float)(Math.atan2(y, hLen) * 180.0D / Math.PI);
    this.ticksInGround = 0;
  }
  
  public void func_70016_h(double x, double y, double z) {
    this.motionX = x;
    this.motionY = y;
    this.motionZ = z;
    if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
      double h = Math.sqrt(x * x + z * z);
      this.field_70126_B = this.rotationYaw = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
      this.field_70127_C = this.rotationPitch = (float)(Math.atan2(y, h) * 180.0D / Math.PI);
      this.field_70127_C = this.rotationPitch;
      this.field_70126_B = this.rotationYaw;
      func_70012_b(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
      this.ticksInGround = 0;
    } 
  }
  
  public void onUpdate() {
    super.onUpdate();
    if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
      double hLen = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
      this.field_70126_B = this.rotationYaw = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
      this.field_70127_C = this.rotationPitch = (float)Math.toDegrees(Math.atan2(this.motionY, hLen));
    } 
    World world = getEntityWorld();
    if (this.fuse-- <= 0) {
      setDead();
      if (!world.isRemote)
        explode(); 
    } else if (this.fuse < 100 && this.fuse % 2 == 0) {
      world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
    } 
    if (this.inGround) {
      this.ticksInGround++;
      if (this.ticksInGround >= 200)
        setDead(); 
      if (this.sticky) {
        this.fuse -= 3;
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        if (!world.func_175623_d(this.stickPos))
          return; 
      } 
    } 
    Vec3d start = func_174791_d();
    Vec3d end = new Vec3d(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
    RayTraceResult result = world.rayTraceBlocks(start, end, false, true, false);
    if (result != null) {
      float remainX = (float)(result.field_72307_f.field_72450_a - this.posX);
      float remainY = (float)(result.field_72307_f.field_72448_b - this.posY);
      float remainZ = (float)(result.field_72307_f.field_72449_c - this.posZ);
      double remDist = Math.sqrt((remainX * remainX + remainY * remainY + remainZ * remainZ));
      this.stickPos = result.getBlockPos();
      this.posX -= remainX / remDist * 0.05D;
      this.posY -= remainY / remDist * 0.05D;
      this.posZ -= remainZ / remDist * 0.05D;
      this.posX += remainX;
      this.posY += remainY;
      this.posZ += remainZ;
      this.motionX *= (0.75F - this.field_70146_Z.nextFloat());
      this.motionY *= -0.30000001192092896D;
      this.motionZ *= (0.75F - this.field_70146_Z.nextFloat());
      this.inGround = true;
    } else {
      this.posX += this.motionX;
      this.posY += this.motionY;
      this.posZ += this.motionZ;
      this.inGround = false;
    } 
    double hMotion = Math.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ);
    this.rotationYaw = (float)Math.toDegrees(Math.atan2(this.motionX, this.motionZ));
    this.rotationPitch = (float)Math.toDegrees(Math.atan2(this.motionY, hMotion));
    for (; this.rotationPitch - this.field_70127_C < -180.0F; this.field_70127_C -= 360.0F);
    for (; this.rotationPitch - this.field_70127_C >= 180.0F; this.field_70127_C += 360.0F);
    for (; this.rotationYaw - this.field_70126_B < -180.0F; this.field_70126_B -= 360.0F);
    for (; this.rotationYaw - this.field_70126_B >= 180.0F; this.field_70126_B += 360.0F);
    this.rotationPitch = this.field_70127_C + (this.rotationPitch - this.field_70127_C) * 0.2F;
    this.rotationYaw = this.field_70126_B + (this.rotationYaw - this.field_70126_B) * 0.2F;
    float f3 = 0.98F;
    float f5 = 0.04F;
    if (func_70090_H()) {
      this.fuse += 2000;
      for (int i1 = 0; i1 < 4; i1++) {
        float f6 = 0.25F;
        world.func_175688_a(EnumParticleTypes.WATER_BUBBLE, this.posX - this.motionX * f6, this.posY - this.motionY * f6, this.posZ - this.motionZ * f6, this.motionX, this.motionY, this.motionZ, new int[0]);
      } 
      f3 = 0.75F;
    } 
    this.motionX *= f3;
    this.motionY *= f3;
    this.motionZ *= f3;
    this.motionY -= f5;
    setPosition(this.posX, this.posY, this.posZ);
  }
  
  public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
    nbttagcompound.func_74774_a("inGround", (byte)(this.inGround ? 1 : 0));
  }
  
  public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    this.inGround = (nbttagcompound.func_74771_c("inGround") == 1);
  }
  
  public void explode() {
    PointExplosion explosion = new PointExplosion(getEntityWorld(), this, this.owner, this.posX, this.posY, this.posZ, 1.0F, 1.0F, 20);
    explosion.func_77278_a();
    explosion.func_77279_a(true);
  }
}
