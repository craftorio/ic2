package ic2.core.entity.boat;

import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import ic2.core.IC2;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Items;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class CarbonBoatEntity extends AbstractBoatEntity
{
	private int movementTicks = 0;
	private static final int MAX_MOVEMENT_TICKS = 60;

	public CarbonBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world)
	{
		super(entityType, world);
	}

	@SuppressWarnings("unused")
	public CarbonBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		/* This method are used by reflection. */
		super(entityType, world, x, y, z);
	}

	@Override
	public boolean brokenByFalling()
	{
		return false;
	}

	@Override
	public @NotNull Item getDropItem()
	{
		return Ic2Items.CARBON_BOAT;
	}

	@Override
	public BoatType getOverrideBoatType()
	{
		return Ic2BoatTypes.CARBON;
	}

	@Override
	protected float getBlockSpeedFactor()
	{
		return (float) (super.getBlockSpeedFactor() * getSpeedMultiplier());
	}

	private double getSpeedMultiplier()
	{
		if (movementTicks <= 0) return 1.0;
		double t = Math.min(movementTicks, MAX_MOVEMENT_TICKS);
		double x = t / MAX_MOVEMENT_TICKS;
		double smoothstep = 3.0 * x * x - 2.0 * x * x * x;
		return 1.0 + 0.15 * smoothstep;
	}

	@Override
	public void tick()
	{
		Entity driver = this.getControllingPassenger();
		if (driver instanceof Player && IC2.keyboard.isForwardKeyDown((Player) driver))
		{
			movementTicks++;
		}
		else
		{
			if (movementTicks > 0)
			{
				if (movementTicks > MAX_MOVEMENT_TICKS)
					movementTicks = MAX_MOVEMENT_TICKS;
				movementTicks--;
			}
		}

		super.tick();

		if (!this.level().isClientSide && this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA))
		{
			this.hurt(this.damageSources().lava(), Float.MAX_VALUE);
		}
	}
}
