package ic2.core.entity.boat;

import ic2.api.entity.boat.AbstractBoatEntity;
import ic2.api.entity.boat.BoatType;
import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.Ic2BoatTypes;
import ic2.core.ref.Ic2Items;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

public class ElectricBoatEntity extends AbstractBoatEntity
{
	private static final double euConsume = 4.0;
	private boolean accelerated = false;

	public ElectricBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world)
	{
		super(entityType, world);
	}

	public ElectricBoatEntity(EntityType<? extends AbstractBoatEntity> entityType, Level world, double x, double y, double z)
	{
		super(entityType, world, x, y, z);
	}

	public Item m_38369_()
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

	public boolean m_5825_()
	{
		return true;
	}

	public boolean m_6060_()
	{
		return false;
	}

	@Override
	public boolean canFloatOn(FluidState fluidState)
	{
		return super.canFloatOn(fluidState);
	}

	protected double getAccelerationFactor()
	{
		return this.accelerated ? 1.5 : 0.25;
	}

	protected float m_6041_()
	{
		return (float) (super.m_6041_() * this.getAccelerationFactor());
	}

	@Override
	public void m_8119_()
	{
		this.accelerated = false;
		Entity driver = this.m_6688_();
		if (driver instanceof Player && IC2.keyboard.isForwardKeyDown((Player) driver))
		{
			for (ItemStack stack : ((Player) driver).getInventory().f_35975_)
			{
				if (!StackUtil.isEmpty(stack) && ElectricItem.manager.discharge(stack, 4.0, Integer.MAX_VALUE, true, true, true) == 4.0)
				{
					ElectricItem.manager.discharge(stack, 4.0, Integer.MAX_VALUE, true, true, false);
					this.accelerated = true;
					break;
				}
			}
		}

		super.m_8119_();
	}
}
