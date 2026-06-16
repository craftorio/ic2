package ic2.core.entity.boat;

import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.NotNull;

public class ElectricBoatEntity extends AbstractBoatEntity
{
	private int movementTicks = 0;
	private int decelTimer = 0;
	private int decelStartTicks = 0;
	private static final int MAX_ACCELERATION_TICKS = 80;
	private static final int MAX_DECEL_TICKS = 40;

	public ElectricBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world)
	{
		super(entityType, world);
	}

	@SuppressWarnings("unused")
	public ElectricBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		/* This method are used by reflection. */
		super(entityType, world, x, y, z);
	}

	public @NotNull Item getDropItem()
	{
		return Ic2Items.ELECTRIC_BOAT;
	}

	@Override
	public BoatType getOverrideBoatType()
	{
		return Ic2BoatTypes.ELECTRIC;
	}

	@Override
	public boolean brokenByFalling()
	{
		return false;
	}

	public boolean fireImmune()
	{
		return true;
	}

	public boolean isOnFire()
	{
		return false;
	}

	@Override
	public boolean canFloatOn(FluidState fluidState)
	{
		return super.canFloatOn(fluidState);
	}

	private double getSpeedMultiplier()
	{
		if (movementTicks <= 0) return 1.0;
		double x = Math.min(movementTicks, MAX_ACCELERATION_TICKS) / (double) MAX_ACCELERATION_TICKS;
		return 1.0 + 0.6 * x;
	}

	@Override
	protected float getBlockSpeedFactor()
	{
		return (float) (super.getBlockSpeedFactor() * getSpeedMultiplier());
	}

	@Override
	public void tick()
	{
		Entity driver = this.getControllingPassenger();
		boolean hasPower = false;
		if (driver instanceof Player && IC2.keyboard.isForwardKeyDown((Player) driver))
		{
			int powerCost = movementTicks < MAX_ACCELERATION_TICKS ? 8 : 4;
			for (ItemStack stack : ((Player) driver).getInventory().armor)
			{
				if (!StackUtil.isEmpty(stack) && ElectricItem.manager.discharge(stack, powerCost, Integer.MAX_VALUE, true, true, true) == powerCost)
				{
					ElectricItem.manager.discharge(stack, powerCost, Integer.MAX_VALUE, true, true, false);
					hasPower = true;
					break;
				}
			}
		}

		if (hasPower)
		{
			decelTimer = 0;
			if (movementTicks < MAX_ACCELERATION_TICKS)
			{
				movementTicks++;
			}
		}
		else if (movementTicks > 0)
		{
			if (decelTimer == 0)
			{
				decelStartTicks = movementTicks;
			}
			decelTimer++;
			double totalDecelTime = Math.max((double) decelStartTicks / MAX_ACCELERATION_TICKS * MAX_DECEL_TICKS, 1.0);
			double progress = Math.min(decelTimer / totalDecelTime, 1.0);
			double smoothstep = 3.0 * progress * progress - 2.0 * progress * progress * progress;
			movementTicks = (int) Math.round(decelStartTicks * (1.0 - smoothstep));
			if (progress >= 1.0)
			{
				movementTicks = 0;
				decelTimer = 0;
			}
		}

		super.tick();

		if (!this.level().isClientSide && this.level().getFluidState(this.blockPosition()).is(FluidTags.LAVA))
		{
			Entity rider = this.getControllingPassenger();
			if (rider != null)
			{
				rider.setSecondsOnFire(5);
			}
		}
	}
}
