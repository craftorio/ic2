package ic2.core.block;

import ic2.core.ExplosionIC2;
import ic2.core.IC2;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.MoverType;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class EntityIC2Explosive extends Entity {
  public DamageSource damageSource;
  
  public EntityLivingBase igniter;
  
  public int fuse;
  
  public float explosivePower;
  
  public int radiationRange;
  
  public float dropRate;
  
  public float damageVsEntitys;
  
  public IBlockState renderBlockState;
  
  public EntityIC2Explosive(World world) {
    super(world);
    this.fuse = 80;
    this.explosivePower = 4.0F;
    this.radiationRange = 0;
    this.dropRate = 0.3F;
    this.damageVsEntitys = 1.0F;
    this.renderBlockState = Blocks.field_150346_d.getDefaultState();
    this.field_70156_m = true;
    func_70105_a(0.98F, 0.98F);
  }
  
  public EntityIC2Explosive(World world, double x, double y, double z, int fuse, float power, float dropRate, float damage, IBlockState renderBlockState, int radiationRange) {
    this(world);
    func_70107_b(x, y, z);
    float f = (float)(Math.random() * 3.1415927410125732D * 2.0D);
    this.field_70159_w = (-MathHelper.func_76126_a(f * 3.141593F / 180.0F) * 0.02F);
    this.field_70181_x = 0.20000000298023224D;
    this.field_70179_y = (-MathHelper.func_76134_b(f * 3.141593F / 180.0F) * 0.02F);
    this.field_70169_q = x;
    this.field_70167_r = y;
    this.field_70166_s = z;
    this.fuse = fuse;
    this.explosivePower = power;
    this.radiationRange = radiationRange;
    this.dropRate = dropRate;
    this.damageVsEntitys = damage;
    this.renderBlockState = renderBlockState;
  }
  
  protected void func_70088_a() {}
  
  protected boolean func_70041_e_() {
    return false;
  }
  
  public boolean func_70067_L() {
    return !this.field_70128_L;
  }
  
  public void func_70071_h_() {
    this.field_70169_q = this.field_70165_t;
    this.field_70167_r = this.field_70163_u;
    this.field_70166_s = this.field_70161_v;
    this.field_70181_x -= 0.04D;
    func_70091_d(MoverType.SELF, this.field_70159_w, this.field_70181_x, this.field_70179_y);
    this.field_70159_w *= 0.98D;
    this.field_70181_x *= 0.98D;
    this.field_70179_y *= 0.98D;
    if (this.field_70122_E) {
      this.field_70159_w *= 0.7D;
      this.field_70179_y *= 0.7D;
      this.field_70181_x *= -0.5D;
    } 
    if (this.fuse-- <= 0) {
      func_70106_y();
      if (IC2.platform.isSimulating())
        explode(); 
    } else {
      func_130014_f_().func_175688_a(EnumParticleTypes.SMOKE_NORMAL, this.field_70165_t, this.field_70163_u + 0.5D, this.field_70161_v, 0.0D, 0.0D, 0.0D, new int[0]);
    } 
  }
  
  private void explode() {
    ExplosionIC2 explosion = new ExplosionIC2(func_130014_f_(), this, this.field_70165_t, this.field_70163_u, this.field_70161_v, this.explosivePower, this.dropRate, (this.radiationRange > 0) ? ExplosionIC2.Type.Nuclear : ExplosionIC2.Type.Normal, this.igniter, this.radiationRange);
    explosion.doExplosion();
  }
  
  protected void func_70014_b(NBTTagCompound nbttagcompound) {
    nbttagcompound.func_74774_a("Fuse", (byte)this.fuse);
  }
  
  protected void func_70037_a(NBTTagCompound nbttagcompound) {
    this.fuse = nbttagcompound.func_74771_c("Fuse");
  }
  
  public EntityIC2Explosive setIgniter(EntityLivingBase igniter1) {
    this.igniter = igniter1;
    return this;
  }
}
