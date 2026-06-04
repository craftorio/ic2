package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.Redstone;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.block.invslot.InvSlotProcessable;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.recipe.BasicMachineRecipeManager;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

@NotClassic
public class TileEntityCentrifuge extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	protected final Redstone redstone;

	public static final short maxHeat = 5000;

	@GuiSynced
	public short heat;

	@GuiSynced
	public short workheat;

	public TileEntityCentrifuge()
	{
		super(48, 500, 3, 2);
		this.heat = 0;
		this.workheat = 5000;
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.centrifuge);
		this.redstone = (Redstone) addComponent((TileEntityComponent) new Redstone(this));
	}

	public static void init()
	{
		Recipes.centrifuge = new BasicMachineRecipeManager();
	}

	public void readFromNBT(NBTTagCompound nbt)
	{
		super.readFromNBT(nbt);
		this.heat = nbt.getShort("heat");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt)
	{
		super.writeToNBT(nbt);
		nbt.setShort("heat", this.heat);
		return nbt;
	}

	public double getHeatRatio()
	{
		return (double) this.heat / this.workheat;
	}

	private static short min(short a, short b)
	{
		return (a <= b) ? a : b;
	}

	protected void updateEntityServer()
	{
		super.updateEntityServer();
		boolean heating = false;
		if (this.energy.canUseEnergy(1.0D))
		{
			short heatRequested = Short.MIN_VALUE;
			MachineRecipeResult<? extends IRecipeInput, ? extends Collection<ItemStack>, ? extends ItemStack> output = super.getOutput();
			if (output != null && !this.redstone.hasRedstoneInput())
			{
				heatRequested = min((short) 5000, output.getRecipe().getMetaData().getShort("minHeat"));
				this.workheat = heatRequested;
				if (this.heat > heatRequested)
					this.heat = heatRequested;
			} else if (this.heat <= 5000 && this.redstone.hasRedstoneInput())
			{
				heatRequested = 5000;
				this.workheat = heatRequested;
			}
			if (this.heat - 1 < heatRequested)
			{
				this.energy.useEnergy(1.0D);
				heating = true;
			}
		}
		if (heating)
		{
			this.heat = (short) (this.heat + 1);
		} else
		{
			this.heat = (short) (this.heat - Math.min(this.heat, 1));
		}
	}

	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getOutput()
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getOutput();
		if (ret != null)
		{
			if (ret.getRecipe().getMetaData() == null)
				return null;
			if (ret.getRecipe().getMetaData().getInteger("minHeat") > this.heat)
				return null;
		}
		return ret;
	}

	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(UpgradableProperty.Processing, UpgradableProperty.RedstoneSensitive, UpgradableProperty.Transformer, UpgradableProperty.EnergyStorage, UpgradableProperty.ItemConsuming, UpgradableProperty.ItemProducing);
	}

	public double getGuiValue(String name)
	{
		if ("heat".equals(name))
			return (double) this.heat / this.workheat;
		return super.getGuiValue(name);
	}
}
