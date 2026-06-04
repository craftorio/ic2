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
    func_70105_a(0.5F, 0.5F);
    func_70107_b(x, y, z);
  }
  
  public EntityDynamite(World world) {
    this(world, 0.0D, 0.0D, 0.0D);
  }
  
  public EntityDynamite(World world, EntityLivingBase owner) {
    super(world);
    this.fuse = 100;
    this.inGround = false;
    this.owner = owner;
    func_70105_a(0.5F, 0.5F);
    Vector3 eyePos = Util.getEyePosition((Entity)owner);
    func_70012_b(eyePos.x, eyePos.y, eyePos.z, owner.field_70177_z, owner.field_70125_A);
    this.field_70165_t -= Math.cos(Math.toRadians(this.field_70177_z)) * 0.16D;
    this.field_70163_u -= 0.1D;
    this.field_70161_v -= Math.sin(Math.toRadians(this.field_70177_z)) * 0.16D;
    func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
    this.field_70159_w = -Math.sin(Math.toRadians(this.field_70177_z)) * Math.cos(Math.toRadians(this.field_70125_A));
    this.field_70179_y = Math.cos(Math.toRadians(this.field_70177_z)) * Math.cos(Math.toRadians(this.field_70125_A));
    this.field_70181_x = -Math.sin(Math.toRadians(this.field_70125_A));
    func_70186_c(this.field_70159_w, this.field_70181_x, this.field_70179_y, 1.0F, 1.0F);
  }
  
  protected void func_70088_a() {}
  
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
    this.field_70159_w = x;
    this.field_70181_x = y;
    this.field_70179_y = z;
    double hLen = Math.sqrt(x * x + z * z);
    this.field_70126_B = this.field_70177_z = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
    this.field_70127_C = this.field_70125_A = (float)(Math.atan2(y, hLen) * 180.0D / Math.PI);
    this.ticksInGround = 0;
  }
  
  public void func_70016_h(double x, double y, double z) {
    this.field_70159_w = x;
    this.field_70181_x = y;
    this.field_70179_y = z;
    if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
      double h = Math.sqrt(x * x + z * z);
      this.field_70126_B = this.field_70177_z = (float)(Math.atan2(x, z) * 180.0D / Math.PI);
      this.field_70127_C = this.field_70125_A = (float)(Math.atan2(y, h) * 180.0D / Math.PI);
      this.field_70127_C = this.field_70125_A;
      this.field_70126_B = this.field_70177_z;
      func_70012_b(this.field_70165_t, this.field_70163_u, this.field_70161_v, this.field_70177_z, this.field_70125_A);
      this.ticksInGround = 0;
    } 
  }
  
  public void func_70071_h_() {
    super.func_70071_h_();
    if (this.field_70127_C == 0.0F && this.field_70126_B == 0.0F) {
      double hLen = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
      this.field_70126_B = this.field_70177_z = (float)Math.toDegrees(Math.atan2(this.field_70159_w, this.field_70179_y));
      this.field_70127_C = this.field_70125_A = (float)Math.toDegrees(Math.atan2(this.field_70181_x, hLen));
    } 
    World world = func_130014_f_();
    if (this.fuse-- <= 0) {
      func_70106_y();
      if (!world.isRemote)
        explode(); 
    } else if (this.fuse < 100 && this.fuse % 2 == 0) {
      world.func_175688_a(EnumParticleTypes.SMOKE_NORMAL, this.field_70165_t, this.field_70163_u + 0.5D, this.field_70161_v, 0.0D, 0.0D, 0.0D, new int[0]);
    } 
    if (this.inGround) {
      this.ticksInGround++;
      if (this.ticksInGround >= 200)
        func_70106_y(); 
      if (this.sticky) {
        this.fuse -= 3;
        this.field_70159_w = 0.0D;
        this.field_70181_x = 0.0D;
        this.field_70179_y = 0.0D;
        if (!world.func_175623_d(this.stickPos))
          return; 
      } 
    } 
    Vec3d start = func_174791_d();
    Vec3d end = new Vec3d(this.field_70165_t + this.field_70159_w, this.field_70163_u + this.field_70181_x, this.field_70161_v + this.field_70179_y);
    RayTraceResult result = world.func_147447_a(start, end, false, true, false);
    if (result != null) {
      float remainX = (float)(result.field_72307_f.field_72450_a - this.field_70165_t);
      float remainY = (float)(result.field_72307_f.field_72448_b - this.field_70163_u);
      float remainZ = (float)(result.field_72307_f.field_72449_c - this.field_70161_v);
      double remDist = Math.sqrt((remainX * remainX + remainY * remainY + remainZ * remainZ));
      this.stickPos = result.func_178782_a();
      this.field_70165_t -= remainX / remDist * 0.05D;
      this.field_70163_u -= remainY / remDist * 0.05D;
      this.field_70161_v -= remainZ / remDist * 0.05D;
      this.field_70165_t += remainX;
      this.field_70163_u += remainY;
      this.field_70161_v += remainZ;
      this.field_70159_w *= (0.75F - this.field_70146_Z.nextFloat());
      this.field_70181_x *= -0.30000001192092896D;
      this.field_70179_y *= (0.75F - this.field_70146_Z.nextFloat());
      this.inGround = true;
    } else {
      this.field_70165_t += this.field_70159_w;
      this.field_70163_u += this.field_70181_x;
      this.field_70161_v += this.field_70179_y;
      this.inGround = false;
    } 
    double hMotion = Math.sqrt(this.field_70159_w * this.field_70159_w + this.field_70179_y * this.field_70179_y);
    this.field_70177_z = (float)Math.toDegrees(Math.atan2(this.field_70159_w, this.field_70179_y));
    this.field_70125_A = (float)Math.toDegrees(Math.atan2(this.field_70181_x, hMotion));
    for (; this.field_70125_A - this.field_70127_C < -180.0F; this.field_70127_C -= 360.0F);
    for (; this.field_70125_A - this.field_70127_C >= 180.0F; this.field_70127_C += 360.0F);
    for (; this.field_70177_z - this.field_70126_B < -180.0F; this.field_70126_B -= 360.0F);
    for (; this.field_70177_z - this.field_70126_B >= 180.0F; this.field_70126_B += 360.0F);
    this.field_70125_A = this.field_70127_C + (this.field_70125_A - this.field_70127_C) * 0.2F;
    this.field_70177_z = this.field_70126_B + (this.field_70177_z - this.field_70126_B) * 0.2F;
    float f3 = 0.98F;
    float f5 = 0.04F;
    if (func_70090_H()) {
      this.fuse += 2000;
      for (int i1 = 0; i1 < 4; i1++) {
        float f6 = 0.25F;
        world.func_175688_a(EnumParticleTypes.WATER_BUBBLE, this.field_70165_t - this.field_70159_w * f6, this.field_70163_u - this.field_70181_x * f6, this.field_70161_v - this.field_70179_y * f6, this.field_70159_w, this.field_70181_x, this.field_70179_y, new int[0]);
      } 
      f3 = 0.75F;
    } 
    this.field_70159_w *= f3;
    this.field_70181_x *= f3;
    this.field_70179_y *= f3;
    this.field_70181_x -= f5;
    func_70107_b(this.field_70165_t, this.field_70163_u, this.field_70161_v);
  }
  
  public void func_70014_b(NBTTagCompound nbttagcompound) {
    nbttagcompound.func_74774_a("inGround", (byte)(this.inGround ? 1 : 0));
  }
  
  public void func_70037_a(NBTTagCompound nbttagcompound) {
    this.inGround = (nbttagcompound.func_74771_c("inGround") == 1);
  }
  
  public void explode() {
    PointExplosion explosion = new PointExplosion(func_130014_f_(), this, this.owner, this.field_70165_t, this.field_70163_u, this.field_70161_v, 1.0F, 1.0F, 20);
    explosion.func_77278_a();
    explosion.func_77279_a(true);
  }
}
