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
import ic2.core.block.machine.gui.GuiCondenser;
import ic2.core.gui.dynamic.IGuiValueProvider;
import ic2.core.profile.NotClassic;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;

import java.util.EnumSet;
import java.util.Set;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityCondenser extends TileEntityElectricMachine implements IHasGui, IGuiValueProvider, IUpgradableBlock
{
	private final short passiveCooling = 100;
	private final short coolingPerVent = 100;
	public final short ventEUCost = 2;
	public int progress = 0;
	public final int maxProgress = 10000;
	private final FluidTank inputTank;
	private final FluidTank outputTank;
	public final InvSlotConsumableLiquidByTank waterInputSlot;
	public final InvSlotOutput waterOutputSlot;
	public final InvSlotConsumableId ventSlots;
	public final InvSlotUpgrade upgradeSlot;
	protected final Fluids fluids = this.addComponent(new Fluids(this));

	public TileEntityCondenser()
	{
		super(100000, 3);
		this.inputTank = this.fluids
			.addTankInsert("inputTank", 100000, Fluids.fluidPredicate(FluidName.steam.getInstance(), FluidName.superheated_steam.getInstance()));
		this.outputTank = this.fluids.addTankExtract("outputTank", 1000);
		this.waterInputSlot = new InvSlotConsumableLiquidByTank(
			this, "waterInputSlot", InvSlot.Access.I, 1, InvSlot.InvSide.BOTTOM, InvSlotConsumableLiquid.OpType.Fill, this.outputTank
		);
		this.waterOutputSlot = new InvSlotOutput(this, "waterOutputSlot", 1);
		this.ventSlots = new InvSlotConsumableId(this, "ventSlots", 4, ItemName.heat_vent.getInstance());
		this.ventSlots.setStackSizeLimit(1);
		this.upgradeSlot = new InvSlotUpgrade(this, "upgradeSlot", 1);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbttagcompound)
	{
		super.readFromNBT(nbttagcompound);
		this.progress = nbttagcompound.getInteger("progress");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setInteger("progress", this.progress);
		return nbt;
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		if (!this.getWorld().isRemote)
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
	public void markDirty()
	{
		super.markDirty();
		if (!this.getWorld().isRemote)
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
			super.markDirty();
		}
	}

	private void work()
	{
		if (this.outputTank.getCapacity() - this.outputTank.getFluidAmount() >= 100)
		{
			if (this.progress >= 10000)
			{
				this.outputTank.fillInternal(new FluidStack(FluidName.distilled_water.getInstance(), 100), true);
				this.progress -= 10000;
			}

			if (this.inputTank.getFluidAmount() > 0)
			{
				byte vents = this.getVents();
				int drain = 100 + vents * 100;
				if (this.energy.useEnergy(vents * 2))
				{
					this.progress = this.progress + this.inputTank.drainInternal(drain, true).amount;
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
	public ContainerBase<TileEntityCondenser> getGuiContainer(EntityPlayer player)
	{
		return new ContainerCondenser(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiCondenser(new ContainerCondenser(player, this));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
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

	public int gaugeLiquidScaled(int i, int tank)
	{
		switch (tank)
		{
			case 0:
				if (this.inputTank.getFluidAmount() <= 0)
				{
					return 0;
				}

				return this.inputTank.getFluidAmount() * i / this.inputTank.getCapacity();
			case 1:
				if (this.outputTank.getFluidAmount() <= 0)
				{
					return 0;
				}

				return this.outputTank.getFluidAmount() * i / this.outputTank.getCapacity();
			default:
				return 0;
		}
	}

	public FluidTank getInputTank()
	{
		return this.inputTank;
	}

	public FluidTank getOutputTank()
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
