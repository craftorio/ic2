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
	private static final EntityDataAccessor<Integer> FUSE = SynchedEntityData.m_135353_(ExplosiveEntity.class, EntityDataSerializers.f_135028_);
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
		this.m_20248_(x, y, z);
		double angle = (Math.PI * 2) * world.random.nextDouble();
		this.m_20334_(-Math.sin(angle) * 0.02, 0.2, -Math.cos(angle) * 0.02);
		this.f_19854_ = x;
		this.f_19855_ = y;
		this.f_19856_ = z;
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
		this.f_19850_ = true;
	}

	protected void m_8097_()
	{
		this.f_19804_.m_135372_(FUSE, 80);
	}

	public boolean m_5829_()
	{
		return !this.m_213877_();
	}

	public void m_8119_()
	{
		if (!this.m_20068_())
		{
			this.m_20256_(this.m_20184_().m_82520_(0.0, -0.04, 0.0));
		}

		this.m_6478_(MoverType.SELF, this.m_20184_());
		this.m_20256_(this.m_20184_().m_82490_(0.98));
		if (this.f_19861_)
		{
			this.m_20256_(this.m_20184_().m_82542_(0.7, -0.5, 0.7));
		}

		int i = this.getFuse() - 1;
		this.setFuse(i);
		if (i <= 0)
		{
			this.m_146870_();
			if (!this.f_19853_.isClientSide)
			{
				this.explode();
			}
		} else
		{
			this.m_20073_();
			if (this.f_19853_.isClientSide)
			{
				this.f_19853_.addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.0, 0.0);
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

	protected MovementEmission m_142319_()
	{
		return MovementEmission.NONE;
	}

	public boolean m_6087_()
	{
		return !this.m_213877_();
	}

	protected void m_7380_(CompoundTag nbt)
	{
		nbt.putShort("Fuse", (short) this.getFuse());
	}

	protected void m_7378_(CompoundTag nbt)
	{
		this.setFuse(nbt.getShort("Fuse"));
	}

	protected float m_6380_(Pose pose, EntityDimensions dimensions)
	{
		return 0.15F;
	}

	public void setFuse(int fuse)
	{
		this.f_19804_.m_135381_(FUSE, fuse);
	}

	public int getFuse()
	{
		return (Integer) this.f_19804_.m_135370_(FUSE);
	}

	public Packet<?> m_5654_()
	{
		return new ClientboundAddEntityPacket(this);
	}

	public ExplosiveEntity setCausingEntity(LivingEntity igniter1)
	{
		this.causingEntity = igniter1;
		return this;
	}
}
