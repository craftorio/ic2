package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.comp.Redstone;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityCentrifuge extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public static final short maxHeat = 5000;
	protected final Redstone redstone;
	@GuiSynced
	public short heat = 0;
	@GuiSynced
	public short workheat = 5000;

	public TileEntityCentrifuge(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.CENTRIFUGE, pos, state, 48, 500, 3, 2);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.centrifuge);
		this.redstone = this.addComponent(new Redstone(this));
	}

	private static short min(short b)
	{
		return (short) 5000 <= b ? (short) 5000 : b;
	}

	@Override
	public void load(CompoundTag nbt)
	{
		super.load(nbt);
		this.heat = nbt.getShort("heat");
	}

	@Override
	public void saveAdditional(CompoundTag nbt)
	{
		super.saveAdditional(nbt);
		nbt.putShort("heat", this.heat);
	}

	public double getHeatRatio()
	{
		return (double) this.heat / this.workheat;
	}

	@Override
	protected void updateEntityServer()
	{
		super.updateEntityServer();
		int energyPerHeat = 1;
		int coolingPerTick = 1;
		boolean heating = false;
		if (this.energy.canUseEnergy(1.0))
		{
			short heatRequested = -32768;
			MachineRecipeResult<? extends IRecipeInput, ? extends Collection<ItemStack>, ? extends ItemStack> output = super.getRecipeResult();
			if (output != null && !this.redstone.hasRedstoneInput())
			{
				heatRequested = min(output.recipe().getMetaData().getShort("minHeat"));
				this.workheat = heatRequested;
				if (this.heat > heatRequested)
				{
					this.heat = heatRequested;
				}
			} else if (this.heat <= 5000 && this.redstone.hasRedstoneInput())
			{
				heatRequested = 5000;
				this.workheat = heatRequested;
			}

			if (this.heat - 1 < heatRequested)
			{
				this.energy.useEnergy(1.0);
				heating = true;
			}
		}

		if (heating)
		{
			this.heat++;
		} else
		{
			this.heat = (short) (this.heat - Math.min(this.heat, 1));
		}
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult()
	{
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getRecipeResult();
		if (ret != null)
		{
			if (ret.recipe().getMetaData() == null)
			{
				return null;
			}

			if (ret.recipe().getMetaData().getInt("minHeat") > this.heat)
			{
				return null;
			}
		}

		return ret;
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.RedstoneSensitive,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}

	@Override
	public double getGuiValue(String name)
	{
		return "heat".equals(name) ? (double) this.heat / this.workheat : super.getGuiValue(name);
	}
}
