package ic2.api.entity.block;

import ic2.core.Ic2Explosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.Entity.MovementEmission;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public abstract class ExplosiveEntity extends Entity
{
	private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.defineId(ExplosiveEntity.class, EntityDataSerializers.INT);
	private static final int DEFAULT_FUSE = 80;
	public LivingEntity causingEntity;
	public float explosivePower = 4.0F;
	public int radiationRange = 0;
	public float dropRate = 0.3F;
	public float damageVsEntities = 1.0F;
	public BlockState renderBlockState;

	public ExplosiveEntity(
		EntityType<? extends Entity> type,
		Level world,
		double x,
		double y,
		double z,
		int fuse,
		float power,
		float dropRate,
		float damage,
		BlockState renderBlockState,
		int radiationRange
	)
	{
		this(type, world);
		this.absMoveTo(x, y, z);
		double angle = (Math.PI * 2) * world.random.nextDouble();
		this.setDeltaMovement(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.xo = x;
		this.yo = y;
		this.zo = z;
		this.setFuse(fuse);
		this.explosivePower = power;
		this.radiationRange = radiationRange;
		this.dropRate = dropRate;
		this.damageVsEntities = damage;
		this.renderBlockState = renderBlockState;
	}

	public ExplosiveEntity(EntityType<? extends Entity> entityType, Level world)
	{
		super(entityType, world);
		this.blocksBuilding = true;
	}

	protected void defineSynchedData()
	{
		this.entityData.define(FUSE, 80);
	}

	public boolean canBeCollidedWith()
	{
		return !this.isRemoved();
	}

	public void tick()
	{
		if (!this.isNoGravity())
		{
			this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
		}

		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.98));
		if (this.onGround)
		{
			this.setDeltaMovement(this.getDeltaMovement().multiply(0.7, -0.5, 0.7));
		}

		int i = this.getFuse() - 1;
		this.setFuse(i);
		if (i <= 0)
		{
			this.discard();
			if (!this.level.isClientSide)
			{
				this.explode();
			}
		} else
		{
			this.updateInWaterStateAndDoFluidPushing();
			if (this.level.isClientSide)
			{
				this.level.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
			}
		}
	}

	private void explode()
	{
		Ic2Explosion explosion = new Ic2Explosion(
			this.getCommandSenderWorld(),
			this,
			this.getX(),
			this.getY(),
			this.getZ(),
			this.explosivePower,
			this.dropRate,
			this.radiationRange > 0 ? Ic2Explosion.Type.Nuclear : Ic2Explosion.Type.Normal,
			this.causingEntity,
			this.radiationRange
		);
		explosion.doExplosion();
	}

	@Nullable
	public LivingEntity getCausingEntity()
	{
		return this.causingEntity;
	}

	protected MovementEmission getMovementEmission()
	{
		return MovementEmission.NONE;
	}

	public boolean isPickable()
	{
		return !this.isRemoved();
	}

	protected void addAdditionalSaveData(CompoundTag nbt)
	{
		nbt.putShort("Fuse", (short) this.getFuse());
	}

	protected void readAdditionalSaveData(CompoundTag nbt)
	{
		this.setFuse(nbt.getShort("Fuse"));
	}

	protected float getEyeHeight(Pose pose, EntityDimensions dimensions)
	{
		return 0.15F;
	}

	public void setFuse(int fuse)
	{
		this.entityData.set(FUSE, fuse);
	}

	public int getFuse()
	{
		return (Integer) this.entityData.get(FUSE);
	}

	public Packet<?> getAddEntityPacket()
	{
		return new ClientboundAddEntityPacket(this);
	}

	public ExplosiveEntity setCausingEntity(LivingEntity igniter1)
	{
		this.causingEntity = igniter1;
		return this;
	}
}
