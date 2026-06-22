package ic2.core.block.machine.tileentity;

import ic2.api.upgrade.IUpgradableBlock;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.comp.Fluids;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableId;
import ic2.core.block.invslot.InvSlotConsumableLiquid;
import ic2.core.block.invslot.InvSlotConsumableLiquidByTank;
import ic2.core.block.invslot.InvSlotOutput;
import ic2.core.block.invslot.InvSlotUpgrade;
import ic2.core.block.machine.container.ContainerCondenser;
import ic2.core.fluid.Ic2FluidStack;
import ic2.core.fluid.Ic2FluidTank;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.network.GrowingBuffer;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Fluids;
import ic2.core.ref.Ic2Items;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityCondenser extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, IUpgradableBlock
{
	public final short ventEUCost = 2;
	public final int maxProgress = 10000;
	public final InvSlotConsumableLiquidByTank waterInputSlot;
	public final InvSlotOutput waterOutputSlot;
	public final InvSlotConsumableId ventSlots;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));
	private final short passiveCooling = 100;
	private final short coolingPerVent = 100;
	private final Ic2FluidTank inputTank;
	private final Ic2FluidTank outputTank;
	public int progress = 0;

	public TileEntityCondenser(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CONDENSER, pos, state, 100000, 3);
		this.inputTank = this.fluids.addTankInsert("inputTank", 100000, Fluids.fluidPredicate(Ic2Fluids.STEAM.still(), Ic2Fluids.SUPERHEATED_STEAM.still()));
		this.outputTank = this.fluids.addTankExtract("outputTank", 1000);
		this.waterInputSlot = new InvSlotConsumableLiquidByTank(
			this, "waterInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.waterOutputSlot = new InvSlotOutput(this, "waterOutputSlot", 1);
		this.ventSlots = new InvSlotConsumableId(this, "ventSlots", 4, Ic2Items.HEAT_VENT);
		this.ventSlots.setStackSizeLimit(1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgradeSlot", 1);
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.progress = nbt.getInt("progress");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putInt("progress", this.progress);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getLevel().isClientSide)
		{
			this.updateTier();
		}
	}

	public byte getVents()
	{
		byte vents = 0;

		for (int slot = 0; slot < this.ventSlots.size(); slot++)
		{
			if (!this.ventSlots.isEmpty(slot))
			{
				vents++;
			}
		}

		return vents;
	}

	@Override
	public void setChanged()
	{
		super.setChanged();
		if (!this.getLevel().isClientSide)
		{
			this.updateTier();
		}
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		this.waterInputSlot.processFromTank(this.outputTank, this.waterOutputSlot);
		this.setActive(this.inputTank.getFluidAmount() > 0);
		this.work();
		if (this.upgradeSlot.tickNoMark())
		{
			super.setChanged();
		}
	}

	private void work()
	{
		if (this.outputTank.getCapacity() - this.outputTank.getFluidAmount() >= 100)
		{
			if (this.progress >= 10000)
			{
				this.outputTank.fillMbUnchecked(Ic2FluidStack.create(Ic2Fluids.DISTILLED_WATER.still(), 100), false);
				this.progress -= 10000;
			}

			if (this.inputTank.getFluidAmount() > 0)
			{
				byte vents = this.getVents();
				int drain = 100 + vents * 100;
				if (this.energy.useEnergy(vents * 2))
				{
					this.progress = this.progress + this.inputTank.drainMbUnchecked(drain, false).getAmountMb();
				}
			}
		}
	}

	private void updateTier()
	{
		this.upgradeSlot.onChanged();
		int tier = this.upgradeSlot.getTier(3);
		this.energy.setSinkTier(tier);
		this.dischargeSlot.setTier(tier);
	}

	@Override
	public ContainerBase<TileEntityCondenser> createServerScreenHandler(int syncId, Player player)
	{
		return new ContainerCondenser(syncId, player.getInventory(), this);
	}

	@Override
	public ContainerBase<?> createClientScreenHandler(int syncId, Inventory inventory, GrowingBuffer data)
	{
		return new ContainerCondenser(syncId, inventory, this);
	}

	@Override
	public double getGuiValue(String name)
	{
		if ("progress".equals(name))
		{
			return this.progress == 0 ? 0.0 : this.progress / 10000.0;
		} else
		{
			throw new IllegalArgumentException("Invalid Gui value: " + name);
		}
	}

	public Ic2FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public Ic2FluidTank getOutputTank()
	{
		return this.outputTank;
	}

	@Override
	public double getEnergy()
	{
		return 0.0;
	}

	@Override
	public boolean useEnergy(double amount)
	{
		return false;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing,
			UpgradableProperty.FluidConsuming,
			UpgradableProperty.FluidProducing,
			UpgradableProperty.Transformer
		);
	}
}
