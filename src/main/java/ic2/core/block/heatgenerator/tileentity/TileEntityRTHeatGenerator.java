package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.heatgenerator.container.ContainerRTHeatGenerator;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.tileentity.TileEntityHeatSourceInventory;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityRTHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	private boolean newActive;
	public final InvSlotConsumable fuelSlot = new InvSlotConsumableItemStack(this, "fuelSlot", 6, new ItemStack(Ic2Items.RTG_PELLET));
	public static final float outputMultiplier = 2.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/radioisotope");

	public TileEntityRTHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.RT_HEAT_GENERATOR, pos, state);
		this.fuelSlot.setStackSizeLimit(1);
		this.newActive = false;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.HeatBuffer > 0)
		{
			this.newActive = true;
		} else
		{
			this.newActive = false;
		}

		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		return maxAmount >= this.getMaxHeatEmittedPerTick() ? this.getMaxHeatEmittedPerTick() : maxAmount;
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int counter = 0;

		for (int i = 0; i < this.fuelSlot.size(); i++)
		{
			if (!this.fuelSlot.isEmpty(i))
			{
				counter++;
			}
		}

		return counter == 0 ? 0 : (int) (Math.pow(2.0, counter - 1) * outputMultiplier);
	}

	@Override
	public ContainerBase<TileEntityRTHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerRTHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerRTHeatGenerator(syncId, inventory, this);
	}
}
