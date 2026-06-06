package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumableItemStack;
import ic2.core.block.invslot.InvSlotDischarge;
import ic2.core.block.kineticgenerator.container.ContainerElectricKineticGenerator;
import ic2.core.block.kineticgenerator.gui.GuiElectricKineticGenertor;
import ic2.core.init.MainConfig;
import ic2.core.item.type.CraftingItemType;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;

import java.util.Collections;
import java.util.EnumSet;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@NotClassic
public class TileEntityElectricKineticGenerator extends TileEntityInventory implements IKineticSource, IHasGui
{
	public final InvSlotConsumableItemStack slotMotor;
	public final InvSlotDischarge dischargeSlot;
	private final float kuPerEU;
	public double ku = 0.0;
	public final int maxKU = 1000;
	protected final Energy energy;

	public TileEntityElectricKineticGenerator()
	{
		this.kuPerEU = 4.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/electric");
		this.slotMotor = new InvSlotConsumableItemStack(this, "slotMotor", 10, ItemName.crafting.getItemStack(CraftingItemType.electric_motor));
		this.slotMotor.setStackSizeLimit(1);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
		this.energy = this.addComponent(Energy.asBasicSink(this, 10000.0, 4).addManagedSlot(this.dischargeSlot));
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.updateDirections();
	}

	@Override
	public void setFacing(EnumFacing facing)
	{
		super.setFacing(facing);
		this.updateDirections();
	}

	private void updateDirections()
	{
		this.energy.setDirections(EnumSet.complementOf(EnumSet.of(this.getFacing())), Collections.emptySet());
	}

	@Override
	public int maxrequestkineticenergyTick(EnumFacing directionFrom)
	{
		return this.drawKineticEnergy(directionFrom, Integer.MAX_VALUE, true);
	}

	@Override
	public int getConnectionBandwidth(EnumFacing side)
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
	public int requestkineticenergy(EnumFacing directionFrom, int requestkineticenergy)
	{
		return this.drawKineticEnergy(directionFrom, requestkineticenergy, false);
	}

	@Override
	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
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
			this.markDirty();
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
				this.markDirty();
				newActive = true;
			}
		}

		this.setActive(newActive);
	}

	@Override
	public ContainerBase<TileEntityElectricKineticGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerElectricKineticGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	@Override
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiElectricKineticGenertor((ContainerElectricKineticGenerator) this.getGuiContainer(player));
	}

	@Override
	public void onGuiClosed(EntityPlayer player)
	{
	}
}
