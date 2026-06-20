package ic2.core.block.beam;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Entity.RemovalReason;
import net.minecraft.world.level.Level;
import net.minecraft.network.syncher.SynchedEntityData;

public class ParticleEntity extends Entity
{
	private static final double initialVelocity = 0.5;
	private static final double slowdown = 0.99;

	public ParticleEntity(EntityType<?> type, Level world)
	{
		super(type, world);
		this.noPhysics = true;
	}

	public ParticleEntity(TileEntityEmitter emitter)
	{
		this(null, emitter.getLevel());
		Direction dir = emitter.getFacing();
		double x = emitter.getBlockPos().getX() + 0.5 + dir.getStepX() * 0.5;
		double y = emitter.getBlockPos().getY() + 0.5 + dir.getStepY() * 0.5;
		double z = emitter.getBlockPos().getZ() + 0.5 + dir.getStepZ() * 0.5;
		this.absMoveTo(x, y, z);
		this.setDeltaMovement(dir.getStepX() * 0.5, dir.getStepY() * 0.5, dir.getStepZ() * 0.5);
	}

	protected void defineSynchedData(SynchedEntityData.Builder builder)
	{
	}

	protected void readAdditionalSaveData(CompoundTag nbttagcompound)
	{
	}

	protected void addAdditionalSaveData(CompoundTag nbttagcompound)
	{
	}


	public void tick()
	{
		this.move(MoverType.SELF, this.getDeltaMovement());
		this.setDeltaMovement(this.getDeltaMovement().scale(0.99));
		if (this.getDeltaMovement().lengthSqr() < 1.0E-4)
		{
			this.remove(RemovalReason.DISCARDED);
		}
	}
}
