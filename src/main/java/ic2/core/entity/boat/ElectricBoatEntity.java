package ic2.core.entity.boat;

import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import ic2.api.item.ElectricItem;
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
	private boolean hasPower = false;

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
	@Override
	protected float getBlockSpeedFactor()
	{
		return super.getBlockSpeedFactor() * (hasPower ? 1.5f : 0.25f);
	}
	
	@Override
	public void tick()
	{
		hasPower = false;
		if (getControllingPassenger() instanceof Player player)
		{
			int powerCost = 4;
			for (ItemStack stack : player.getInventory().armor)
			{
				if (!StackUtil.isEmpty(stack) && ElectricItem.manager.discharge(stack, powerCost, Integer.MAX_VALUE, true, true, true) == powerCost)
				{
					ElectricItem.manager.discharge(stack, powerCost, Integer.MAX_VALUE, true, true, false);
					hasPower = true;
					break;
				}
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
