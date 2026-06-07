package ic2.core.block.machine.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.block.wiring.tileentity.TileEntityElectricBlock;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityClassicElectrolyzer extends TileEntityBase implements IHasGui
{
	public TileEntityElectricBlock mfe = null;
	public int ticker = IC2.random.nextInt(16);
	public final InvSlotConsumable waterSlot = new InvSlotConsumableId(this, "water", InvSlot.Access.IO, 1, InvSlot.InvSide.TOP, Ic2Items.WATER_CELL);
	public final InvSlotConsumable hydrogenSlot = new InvSlotConsumableId(
		this, "hydrogen", InvSlot.Access.IO, 1, InvSlot.InvSide.BOTTOM, Ic2Items.ELECTROLYZED_WATER_CELL
	);
	@GuiSynced
	protected final Energy energy = this.addComponent(new Energy(this, 20000.0, Util.noFacings, Util.noFacings, 1));

	public TileEntityClassicElectrolyzer(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CLASSIC_ELECTROLYZER, pos, state);
		this.comparator.setUpdate(this.energy::getComparatorValue);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		boolean turnActive = false;
		if (++this.ticker % 16 == 0)
		{
			this.mfe = this.lookForMFE();
		}

		if (this.mfe != null)
		{
			if (this.shouldDrain() && this.canDrain())
			{
				needsInvUpdate |= this.drain();
				turnActive = true;
			}

			if (this.shouldPower() && (this.canPower() || this.energy.getEnergy() > 0.0))
			{
				needsInvUpdate |= this.power();
				turnActive = true;
			}

			this.setActiveState(turnActive, false);
			if (needsInvUpdate)
			{
				this.setChanged();
			}
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_ELECTROLYZER_LOOP;
	}

	public boolean shouldDrain()
	{
		return this.mfe != null && this.mfe.energy.getFillRatio() >= 0.7;
	}

	public boolean shouldPower()
	{
		return this.mfe != null && this.mfe.energy.getFillRatio() <= 0.3;
	}

	public boolean canDrain()
	{
		return this.waterSlot.consume(1, true, false) != null
			&& (
			this.hydrogenSlot.isEmpty()
				|| StackUtil.getSize(this.hydrogenSlot.get()) < Math.min(this.hydrogenSlot.getStackSizeLimit(), this.hydrogenSlot.get().getMaxStackSize())
		);
	}

	public boolean canPower()
	{
		return this.hydrogenSlot.consume(1, true, false) != null
			&& (
			this.waterSlot.isEmpty() || StackUtil.getSize(this.waterSlot.get()) < Math.min(this.waterSlot.getStackSizeLimit(), this.waterSlot.get().getMaxStackSize())
		);
	}

	public boolean drain()
	{
		double amount = this.processRate();
		if (!this.mfe.energy.useEnergy(amount))
		{
			return false;
		}

		this.energy.addEnergy(amount);
		if (this.energy.useEnergy(20000.0))
		{
			this.waterSlot.consume(1);
			if (this.hydrogenSlot.isEmpty())
			{
				this.hydrogenSlot.put(new ItemStack(Ic2Items.ELECTROLYZED_WATER_CELL));
			} else
			{
				this.hydrogenSlot.put(StackUtil.incSize(this.hydrogenSlot.get()));
			}

			return true;
		} else
		{
			return false;
		}
	}

	public boolean power()
	{
		if (this.energy.getEnergy() > 0.0)
		{
			double out = Math.min(this.energy.getEnergy(), this.processRate());
			this.energy.useEnergy(out);
			this.mfe.energy.addEnergy(out);
			return false;
		}

		this.energy.forceAddEnergy(12000 + 2000 * this.mfe.energy.getSinkTier());
		this.hydrogenSlot.consume(1);
		if (this.waterSlot.isEmpty())
		{
			this.waterSlot.put(new ItemStack(Ic2Items.WATER_CELL));
		} else
		{
			this.waterSlot.put(StackUtil.incSize(this.waterSlot.get()));
		}

		return true;
	}

	public int processRate()
	{
		return switch (this.mfe.energy.getSinkTier())
		{
			case 2 -> 8;
			case 3 -> 32;
			case 4 -> 128;
			default -> 2;
		};
	}

	public TileEntityElectricBlock lookForMFE()
	{
		Level world = this.getLevel();

		for (Direction dir : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(dir));
			if (te instanceof TileEntityElectricBlock)
			{
				return (TileEntityElectricBlock) te;
			}
		}

		return null;
	}

	@Override
	public ContainerBase<?> createServerScreenHandler(int syncId, Player player)
	{
		return DynamicContainer.create(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return DynamicContainer.create(syncId, inventory, this);
	}
}
