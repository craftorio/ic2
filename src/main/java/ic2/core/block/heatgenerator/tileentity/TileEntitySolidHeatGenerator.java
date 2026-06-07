package ic2.core.block.heatgenerator.tileentity;

import ic2.core.ContainerBase;
import ic2.core.IC2;
import ic2.core.IHasGui;
import ic2.core.block.invslot.InvSlotConsumableFuel;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.tileentity.TileEntityHeatSourceInventory;
import ic2.core.gui.dynamic.DynamicContainer;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.init.MainConfig;
import ic2.core.network.GrowingBuffer;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.util.ConfigUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntitySolidHeatGenerator extends TileEntityHeatSourceInventory implements IHasGui, IGuiValueProvider
{
	private int heatbuffer = 0;
	public int activityMeter = 0;
	public int ticksSinceLastActiveUpdate;
	@GuiSynced
	public int fuel = 0;
	@GuiSynced
	public int itemFuelTime = 0;
	public final InvSlotConsumableFuel fuelSlot = new InvSlotConsumableFuel(this, "fuel", 1, false);
	public final InvSlotOutput outputslot = new InvSlotOutput(this, "output", 1);
	public static final int emittedHU = Math.round(20.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/heatgenerator/solid"));

	public TileEntitySolidHeatGenerator(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.SOLID_HEAT_GENERATOR, pos, state);
		this.ticksSinceLastActiveUpdate = IC2.random.nextInt(256);
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

		boolean newActive = this.gainheat();
		if (needsInvUpdate)
		{
			this.setChanged();
		}

		if (!this.delayActiveUpdate())
		{
			this.setActive(newActive);
		} else
		{
			if (this.ticksSinceLastActiveUpdate % 256 == 0)
			{
				this.setActive(this.activityMeter > 0);
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

	public boolean gainheat()
	{
		if (this.isConverting())
		{
			this.heatbuffer = this.heatbuffer + this.getMaxHeatEmittedPerTick();
			this.fuel--;
			if (this.fuel == 0 && (int) (Math.random() * 2.0) == 1)
			{
				this.outputslot.add(new ItemStack(Ic2Items.ASHES));
			}

			return true;
		} else
		{
			return false;
		}
	}

	public boolean needsFuel()
	{
		return this.fuel <= 0 && this.getHeatBuffer() == 0;
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

	public boolean delayActiveUpdate()
	{
		return false;
	}

	public boolean gainFuel()
	{
		if (this.outputslot.canAdd(new ItemStack(Ic2Items.ASHES)))
		{
			int fuelValue = this.fuelSlot.consumeFuel() / 4;
			if (fuelValue == 0)
			{
				return false;
			}

			this.fuel += fuelValue;
			this.itemFuelTime = fuelValue;
			return true;
		} else
		{
			return false;
		}
	}

	public boolean isConverting()
	{
		return this.fuel > 0;
	}

	@Override
	protected int fillHeatBuffer(int maxAmount)
	{
		if (this.heatbuffer - maxAmount >= 0)
		{
			this.heatbuffer -= maxAmount;
			return maxAmount;
		} else
		{
			maxAmount = this.heatbuffer;
			this.heatbuffer = 0;
			return maxAmount;
		}
	}

	@Override
	public int getMaxHeatEmittedPerTick()
	{
		return emittedHU;
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
	public double getGuiValue(String name)
	{
		if ("fuel".equals(name))
		{
			return this.fuel == 0 ? 0.0 : (double) this.fuel / this.itemFuelTime;
		} else
		{
			throw new IllegalArgumentException("Unexpected value requested: " + name);
		}
	}
}
