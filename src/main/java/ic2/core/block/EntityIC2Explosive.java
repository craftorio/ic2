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

public class EntityIC2Explosive extends Entity
{
	public DamageSource damageSource;
	public EntityLivingBase igniter;
	public int fuse = 80;
	public float explosivePower = 4.0F;
	public int radiationRange = 0;
	public float dropRate = 0.3F;
	public float damageVsEntitys = 1.0F;
	public IBlockState renderBlockState = Blocks.DIRT.getDefaultState();

	public EntityIC2Explosive(World world)
	{
		super(world);
		this.preventEntitySpawning = true;
		this.setSize(0.98F, 0.98F);
	}

	public EntityIC2Explosive(
		World world, double x, double y, double z, int fuse, float power, float dropRate, float damage, IBlockState renderBlockState, int radiationRange
	)
	{
		this(world);
		this.setPosition(x, y, z);
		float f = (float) (Math.random() * (float) Math.PI * 2.0);
		this.motionX = -MathHelper.sin(f * 3.141593F / 180.0F) * 0.02F;
		this.motionY = 0.2F;
		this.motionZ = -MathHelper.cos(f * 3.141593F / 180.0F) * 0.02F;
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

	protected void entityInit()
	{
	}

	protected boolean canTriggerWalking()
	{
		return false;
	}

	public boolean canBeCollidedWith()
	{
		return !this.isDead;
	}

	public void onUpdate()
	{
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
		this.motionY -= 0.04;
		this.move(MoverType.SELF, this.motionX, this.motionY, this.motionZ);
		this.motionX *= 0.98;
		this.motionY *= 0.98;
		this.motionZ *= 0.98;
		if (this.onGround)
		{
			this.motionX *= 0.7;
			this.motionZ *= 0.7;
			this.motionY *= -0.5;
		}

		if (this.fuse-- <= 0)
		{
			this.setDead();
			if (IC2.platform.isSimulating())
			{
				this.explode();
			}
		} else
		{
			this.getEntityWorld()
				.spawnParticle(EnumParticleTypes.SMOKE_NORMAL, this.posX, this.posY + 0.5, this.posZ, 0.0, 0.0, 0.0, new int[0]);
		}
	}

	private void explode()
	{
		ExplosionIC2 explosion = new ExplosionIC2(
			this.getEntityWorld(),
			this,
			this.posX,
			this.posY,
			this.posZ,
			this.explosivePower,
			this.dropRate,
			this.radiationRange > 0 ? ExplosionIC2.Type.Nuclear : ExplosionIC2.Type.Normal,
			this.igniter,
			this.radiationRange
		);
		explosion.doExplosion();
	}

	protected void writeEntityToNBT(NBTTagCompound nbttagcompound)
	{
		nbttagcompound.setByte("Fuse", (byte) this.fuse);
	}

	protected void readEntityFromNBT(NBTTagCompound nbttagcompound)
	{
		this.fuse = nbttagcompound.getByte("Fuse");
	}

	public EntityIC2Explosive setIgniter(EntityLivingBase igniter1)
	{
		this.igniter = igniter1;
		return this;
	}
}
