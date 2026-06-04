package ic2.core.block.kineticgenerator.tileentity;

import ic2.api.energy.tile.IKineticSource;
import ic2.core.ContainerBase;
import ic2.core.IHasGui;
import ic2.core.block.TileEntityInventory;
import ic2.core.block.comp.Energy;
import ic2.core.block.comp.TileEntityComponent;
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
	public InvSlotConsumableItemStack slotMotor;

	public InvSlotDischarge dischargeSlot;

	private final float kuPerEU;

	public double ku;

	public final int maxKU;

	protected final Energy energy;

	public TileEntityElectricKineticGenerator()
	{
		this.ku = 0.0D;
		this.maxKU = 1000;
		this.kuPerEU = 4.0F * ConfigUtil.getFloat(MainConfig.get(), "balance/energy/kineticgenerator/electric");
		this.slotMotor = new InvSlotConsumableItemStack(this, "slotMotor", 10, ItemName.crafting.getItemStack((Enum) CraftingItemType.electric_motor));
		this.slotMotor.setStackSizeLimit(1);
		this.dischargeSlot = new InvSlotDischarge(this, InvSlot.Access.NONE, 4);
		this.energy = (Energy) addComponent((TileEntityComponent) Energy.asBasicSink(this, 10000.0D, 4).addManagedSlot(this.dischargeSlot));
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		updateDirections();
	}

	public void setFacing(EnumFacing facing)
	{
		super.setFacing(facing);
		updateDirections();
	}

	private void updateDirections()
	{
		this.energy.setDirections(EnumSet.complementOf((EnumSet) EnumSet.of(getFacing())), Collections.emptySet());
	}

	public int maxrequestKineticEnergyTick(EnumFacing directionFrom)
	{
		return drawKineticEnergy(directionFrom, 2147483647, true);
	}

	public int getConnectionBandwidth(EnumFacing side)
	{
		if (side != getFacing())
			return 0;
		return getMaxKU();
	}

	public int getMaxKU()
	{
		int counter = 0;
		int a = getMaxKUForGUI() / 10;
		for (int i = 0; i < this.slotMotor.size(); i++)
		{
			if (!this.slotMotor.isEmpty(i))
				counter += a;
		}
		return counter;
	}

	public int getMaxKUForGUI()
	{
		return 1000;
	}

	public int requestKineticEnergy(EnumFacing directionFrom, int requestKineticEnergy)
	{
		return drawKineticEnergy(directionFrom, requestKineticEnergy, false);
	}

	public int drawKineticEnergy(EnumFacing side, int request, boolean simulate)
	{
		if (side != getFacing())
			return 0;
		int max = (int) Math.min(getMaxKU(), this.ku);
		int out = Math.min(request, max);
		if (!simulate)
		{
			this.ku -= out;
			markDirty();
		}
		return out;
	}

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean newActive = false;
		if (1000.0D - this.ku > 1.0D)
		{
			double max = Math.min(1000.0D - this.ku, this.energy.getEnergy() * this.kuPerEU);
			this.energy.useEnergy(max / this.kuPerEU);
			this.ku += max;
			if (max > 0.0D)
			{
				markDirty();
				newActive = true;
			}
		}
		setActive(newActive);
	}

	public ContainerBase<TileEntityElectricKineticGenerator> getGuiContainer(EntityPlayer player)
	{
		return new ContainerElectricKineticGenerator(player, this);
	}

	@SideOnly(Side.CLIENT)
	public GuiScreen getGui(EntityPlayer player, boolean isAdmin)
	{
		return new GuiElectricKineticGenertor((ContainerElectricKineticGenerator) getGuiContainer(player));
	}

	public void onGuiClosed(EntityPlayer player)
	{
	}
}
