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
    setSize(0.98F, 0.98F);
  }
  
  public EntityIC2Explosive(World world, double x, double y, double z, int fuse, float power, float dropRate, float damage, IBlockState renderBlockState, int radiationRange) {
    this(world);
    setPosition(x, y, z);
    float f = (float)(Math.random() * 3.1415927410125732D * 2.0D);
    this.motionX = (-MathHelper.sin(f * 3.141593F / 180.0F) * 0.02F);
    this.motionY = 0.20000000298023224D;
    this.motionZ = (-MathHelper.cos(f * 3.141593F / 180.0F) * 0.02F);
    this.prevPosX = x;
    this.prevPosY = y;
    this.prevPosZ = z;
    this.fuse = fuse;
    this.explosivePower = power;
    this.radiationRange = radiationRange;
    this.dropRate = dropRate;
    this.damageVsEntitys = damage;
    this.renderBlockState = renderBlockState;
  }
  
  protected void entityInit() {}
  
  protected boolean func_70041_e_() {
    return false;
  }
  
  public boolean func_70067_L() {
    return !this.field_70128_L;
  }
  
  public void onUpdate() {
    this.prevPosX = this.posX;
    this.prevPosY = this.posY;
    this.prevPosZ = this.posZ;
    this.motionY -= 0.04D;
    move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
    this.motionX *= 0.98D;
    this.motionY *= 0.98D;
    this.motionZ *= 0.98D;
    if (this.field_70122_E) {
      this.motionX *= 0.7D;
      this.motionZ *= 0.7D;
      this.motionY *= -0.5D;
    } 
    if (this.fuse-- <= 0) {
      setDead();
      if (IC2.platform.isSimulating())
        explode(); 
    } else {
      getEntityWorld().func_175688_a(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5D, this.posZ, 0.0D, 0.0D, 0.0D, new int[0]);
    } 
  }
  
  private void explode() {
    ExplosionIC2 explosion = new ExplosionIC2(getEntityWorld(), this, this.posX, this.posY, this.posZ, this.explosivePower, this.dropRate, (this.radiationRange > 0) ? ExplosionIC2.Type.Nuclear : ExplosionIC2.Type.Normal, this.igniter, this.radiationRange);
    explosion.doExplosion();
  }
  
  protected void writeEntityToNBT(NBTTagCompound nbttagcompound) {
    nbttagcompound.setByte("Fuse", (byte)this.fuse);
  }
  
  protected void readEntityFromNBT(NBTTagCompound nbttagcompound) {
    this.fuse = nbttagcompound.getByte("Fuse");
  }
  
  public EntityIC2Explosive setIgniter(EntityLivingBase igniter1) {
    this.igniter = igniter1;
    return this;
  }
}
