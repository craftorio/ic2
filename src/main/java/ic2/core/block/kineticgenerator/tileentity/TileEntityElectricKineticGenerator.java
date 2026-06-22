package ic2.core.block.kineticgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.init.IC2Config;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;

import java.util.Collections;
import java.util.EnumSet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityElectricKineticGenerator extends TileEntityAbstractKineticGenerator implements IHasGui
{
	public final int maxKU = 1000;
	protected final Energy energy;
	private final float kuPerEU;
	public InvSlotConsumableItemStack slotMotor;
	public InvSlotDischarge dischargeSlot;
	public double ku = 0.0;

	public TileEntityElectricKineticGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.ELECTRIC_KINETIC_GENERATOR, pos, state);
		this.kuPerEU = 4.0F * (float) IC2Config.balance.energy.kineticGenerator.electric.get().floatValue();
		this.slotMotor = new InvSlotConsumableItemStack(this, "slotMotor", 10, new ItemStack(Ic2Items.ELECTRIC_MOTOR));
		this.slotMotor.setStackSizeLimit(1);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.updateDirections();
	}

	@Override
	protected void setFacing(Level world, Direction facing)
	{
		super.setFacing(world, facing);
		this.updateDirections();
	}

	private void updateDirections()
	{
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), Collections.emptySet());
	}

	@Override
	public int maxrequestkineticenergyTick(Direction directionFrom)
	{
		return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
	}

	@Override
	public int getConnectionBandwidth(Direction side)
	{
		return side != this.getFacing() ? 0 : this.getMaxKU();
	}

	public int getMaxKU()
	{
		int counter = 0;
		int a = this.getMaxKUForGUI() / 10;

		for (int i = 0; i < this.slotMotor.size(); i++)
		{
			if (!this.slotMotor.isEmpty(i))
			{
				counter += a;
			}
		}

		return counter;
	}

	public int getMaxKUForGUI()
	{
		return 1000;
	}

	@Override
	public int drawKineticEnergy(Direction side, int request, boolean simulate)
	{
		if (side != this.getFacing())
		{
			return 0;
		}

		int max = (int) Math.min(this.getMaxKU(), this.ku);
		int out = Math.min(request, max);
		if (!simulate)
		{
			this.ku -= out;
			this.setChanged();
		}

		return out;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean newActive = false;
		if (1000.0 - this.ku > 1.0)
		{
			double max = Math.min(1000.0 - this.ku, this.energy.getEnergy() * this.kuPerEU);
			this.energy.useEnergy(max / this.kuPerEU);
			this.ku += max;
			if (max > 0.0)
			{
				this.setChanged();
				newActive = true;
			}
		}

		this.setActive(newActive);
	}

	@Override
	public ContainerBase<TileEntityElectricKineticGenerator> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerElectricKineticGenerator(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerElectricKineticGenerator(syncId, inventory, this);
	}
}
