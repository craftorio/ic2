// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block;

import net.minecraft.nbt.NBTTagCompound;
import ic2.core.ExplosionIC2;
import net.minecraft.util.EnumParticleTypes;
import ic2.core.IC2;
import net.minecraft.entity.MoverType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.Entity;

public class EntityIC2Explosive extends Entity
{
    public DamageSource damageSource;
    public EntityLivingBase igniter;
    public int fuse;
    public float explosivePower;
    public int radiationRange;
    public float dropRate;
    public float damageVsEntitys;
    public IBlockState renderBlockState;
    
    public EntityIC2Explosive(final World world) {
        super(world);
        this.fuse = 80;
        this.explosivePower = 4.0f;
        this.radiationRange = 0;
        this.dropRate = 0.3f;
        this.damageVsEntitys = 1.0f;
        this.renderBlockState = Blocks.DIRT.getDefaultState();
        this.preventEntitySpawning = true;
        this.setSize(0.98f, 0.98f);
    }
    
    public EntityIC2Explosive(final World world, final double x, final double y, final double z, final int fuse, final float power, final float dropRate, final float damage, final IBlockState renderBlockState, final int radiationRange) {
        this(world);
        this.setPosition(x, y, z);
        final float f = (float)(Math.random() * 3.1415927410125732 * 2.0);
        this.motionX = -MathHelper.sin(f * 3.141593f / 180.0f) * 0.02f;
        this.motionY = 0.20000000298023224;
        this.motionZ = -MathHelper.cos(f * 3.141593f / 180.0f) * 0.02f;
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
    
    protected void entityInit() {
    }
    
    protected boolean canTriggerWalking() {
        return false;
    }
    
    public boolean canBeCollidedWith() {
        return !this.isDead;
    }
    
    public void onUpdate() {
        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
        this.motionY -= 0.04;
        this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
        this.motionX *= 0.98;
        this.motionY *= 0.98;
        this.motionZ *= 0.98;
        if (this.onGround) {
            this.motionX *= 0.7;
            this.motionZ *= 0.7;
            this.motionY *= -0.5;
        }
        if (this.fuse-- <= 0) {
            this.setDead();
            if (IC2.platform.isSimulating()) {
                this.explode();
            }
        }
        else {
            this.getEntityWorld().spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0, new int[0]);
        }
    }
    
    private void explode() {
        final ExplosionIC2 explosion = new ExplosionIC2(this.getEntityWorld(), this, this.posX, this.posY, this.posZ, this.explosivePower, this.dropRate, (this.radiationRange > 0) ? ExplosionIC2.Type.Nuclear : ExplosionIC2.Type.Normal, this.igniter, this.radiationRange);
        explosion.doExplosion();
    }
    
    protected void writeEntityToNBT(final NBTTagCompound nbttagcompound) {
        nbttagcompound.setByte("Fuse", (byte)this.fuse);
    }
    
    protected void readEntityFromNBT(final NBTTagCompound nbttagcompound) {
        this.fuse = nbttagcompound.getByte("Fuse");
    }
    
    public EntityIC2Explosive setIgniter(final EntityLivingBase igniter1) {
        this.igniter = igniter1;
        return this;
    }
}
