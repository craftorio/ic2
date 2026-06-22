package ic2.core.block.generator.tileentity;

import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.init.IC2Config;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRTGenerator extends TileEntityBaseGenerator
{
	public final InvSlotConsumable fuelSlot = new InvSlotConsumableItemStack(this, "fuel", 6, new ItemStack(Ic2Items.RTG_PELLET));

	public TileEntityRTGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.RT_GENERATOR, pos, state, Math.round(16.0F * (float) IC2Config.balance.energy.generator.radioisotope.get().floatValue()), 1, 20000);
		this.fuelSlot.setStackSizeLimit(1);
	}

	@Override
	public boolean gainEnergy()
	{
		int counter = 0;

		for (int i = 0; i < this.fuelSlot.size(); i++)
		{
			if (!this.fuelSlot.isEmpty(i))
			{
				counter++;
			}
		}

		if (counter == 0)
		{
			return false;
		}

		this.energy.addEnergy(Math.pow(2.0, counter - 1) * (float) IC2Config.balance.energy.generator.radioisotope.get().floatValue());
		return true;
	}

	@Override
	public boolean gainFuel()
	{
		return false;
	}

	@Override
	public boolean needsFuel()
	{
		return false;
	}

	@Override
	protected boolean delayActiveUpdate()
	{
		return true;
	}
}
