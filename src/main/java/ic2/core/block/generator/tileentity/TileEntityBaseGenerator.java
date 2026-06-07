package ic2.core.block.generator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlotCharge;
import ic2.core.block.tileentity.TileEntityBase;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class TileEntityBaseGenerator extends TileEntityBase implements IHasGui
{
	public final InvSlotCharge chargeSlot;
	protected final Energy energy;
	@GuiSynced
	public int fuel = 0;
	protected double production;
	private int ticksSinceLastActiveUpdate;
	private int activityMeter = 0;

	public TileEntityBaseGenerator(
		BlockEntityType<? extends TileEntityBaseGenerator> type, BlockPos pos, BlockState state, double production, int tier, int maxStorage
	)
	{
		super(type, pos, state);
		this.production = production;
		this.ticksSinceLastActiveUpdate = IC2.random.nextInt(256);
		this.chargeSlot = new InvSlotCharge(this, 1);
		this.energy = this.addComponent(Energy.asBasicSource(this, maxStorage, tier).addManagedSlot(this.chargeSlot));
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.fuel = nbt.getInt("fuel");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("fuel", this.fuel);
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean needsInvUpdate = false;
		if (this.needsFuel())
		{
			needsInvUpdate = this.gainFuel();
		}

		boolean newActive = this.gainEnergy();
		if (needsInvUpdate)
		{
			this.setChanged();
		}

		if (!this.delayActiveUpdate())
		{
			this.setActiveState(newActive, false);
		} else
		{
			if (this.ticksSinceLastActiveUpdate % 256 == 0)
			{
				this.setActiveState(this.activityMeter > 0, false);
				this.activityMeter = 0;
			}

			if (newActive)
			{
				this.activityMeter++;
			} else
			{
				this.activityMeter--;
			}

			this.ticksSinceLastActiveUpdate++;
		}
	}

	public boolean gainEnergy()
	{
		if (this.isConverting())
		{
			this.energy.addEnergy(this.production);
			this.fuel--;
			return true;
		} else
		{
			return false;
		}
	}

	public boolean isConverting()
	{
		return !this.needsFuel() && this.energy.getFreeEnergy() >= this.production;
	}

	public boolean needsFuel()
	{
		return this.fuel <= 0 && this.energy.getFreeEnergy() >= this.production;
	}

	public abstract boolean gainFuel();

	protected boolean delayActiveUpdate()
	{
		return false;
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

	@Override
	public void onNetworkUpdate(String field)
	{
		super.onNetworkUpdate(field);
	}
}
