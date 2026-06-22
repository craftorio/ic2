package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.heatgenerator.container.ContainerElectricHeatGenerator;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.tileentity.TileEntityHeatSourceInventory;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityElectricHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui
{
	public static final double outputMultiplier = IC2Config.balance.energy.heatGenerator.electric.get();
	public final InvSlotDischarge dischargeSlot;
	public final InvSlotConsumable coilSlot = new InvSlotConsumableItemStack(this, "CoilSlot", 10, new ItemStack(Ic2Items.COIL));
	protected final Energy energy;
	private boolean newActive;

	public TileEntityElectricHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ELECTRIC_HEAT_GENERATOR, pos, state);
		this.coilSlot.setStackSizeLimit(1);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
		this.newActive = false;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		if (this.getActive() != this.newActive)
		{
			this.setActive(this.newActive);
		}
	}

	@Override
	public ContainerBase<TileEntityElectricHeatGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerElectricHeatGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectricHeatGenerator(syncId, inventory, this);
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		int amount = Math.min(maxAmount, (int) (this.energy.getEnergy() / outputMultiplier));
		if (amount > 0)
		{
			this.energy.useEnergy(amount / outputMultiplier);
			this.newActive = true;
		} else
		{
			this.newActive = false;
		}

		return amount;
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		int counter = 0;

		for (int i = 0; i < this.coilSlot.size(); i++)
		{
			if (!this.coilSlot.isEmpty(i))
			{
				counter++;
			}
		}

		return counter * 10;
	}

	public final float getChargeLevel()
	{
		return (float) Math.min(1.0, this.energy.getFillRatio());
	}
}
