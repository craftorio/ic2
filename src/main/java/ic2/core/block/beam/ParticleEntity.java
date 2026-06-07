package ic2.core.block.beam;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;

public class ParticleEntity extends Entity
{
	private static final double initialVelocity = 0.5;
	private static final double slowdown = 0.99;

	public ParticleEntity(EntityType<?> type, Level world)
	{
		super(type, world);
		this.f_19794_ = true;
	}

	public ParticleEntity(TileEntityEmitter emitter)
	{
		this(null, emitter.getLevel());
		Direction dir = emitter.getFacing();
		double x = emitter.getBlockPos().getX() + 0.5 + dir.m_122429_() * 0.5;
		double y = emitter.getBlockPos().getY() + 0.5 + dir.m_122430_() * 0.5;
		double z = emitter.getBlockPos().getZ() + 0.5 + dir.m_122431_() * 0.5;
		this.m_20248_(x, y, z);
		this.m_20334_(dir.m_122429_() * 0.5, dir.m_122430_() * 0.5, dir.m_122431_() * 0.5);
	}

	protected void m_8097_()
	{
	}

	protected void m_7378_(CompoundTag nbttagcompound)
	{
	}

	protected void m_7380_(CompoundTag nbttagcompound)
	{
	}

	public Packet<?> m_5654_()
	{
		return new ClientboundAddEntityPacket(this);
	}

	public void m_8119_()
	{
		this.m_6478_(MoverType.SELF, this.m_20184_());
		this.m_20256_(this.m_20184_().m_82490_(0.99));
		if (this.m_20184_().m_82556_() < 1.0E-4)
		{
			this.m_142687_(RemovalReason.DISCARDED);
		}
	}
}
