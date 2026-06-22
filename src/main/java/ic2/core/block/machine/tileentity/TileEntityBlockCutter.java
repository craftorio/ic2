package ic2.core.block.machine.tileentity;

import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotConsumableClass;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.network.GuiSynced;
import ic2.core.profile.NotClassic;
import ic2.core.ref.Ic2BlockEntities;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

@NotClassic
public class TileEntityBlockCutter extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public final InvSlotConsumableClass cutterSlot;
	@GuiSynced
	private boolean bladeTooWeak = false;

	public TileEntityBlockCutter(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.BLOCK_CUTTER, pos, state, 4, 450, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.block_cutter);
		this.cutterSlot = new InvSlotConsumableClass(this, "cutter_input_slot", 1, IBlockCuttingBlade.class);
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult()
	{
		if (this.cutterSlot.isEmpty())
		{
			if (!this.bladeTooWeak)
			{
				this.bladeTooWeak = true;
			}

			return null;
		} else
		{
			if (this.bladeTooWeak)
			{
				this.bladeTooWeak = false;
			}

			MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> ret = super.getRecipeResult();
			if (ret != null && ret.getRecipe().getMetaData() != null)
			{
				ItemStack bladeStack = this.cutterSlot.get();
				IBlockCuttingBlade blade = (IBlockCuttingBlade) bladeStack.getItem();
				if (ret.getRecipe().getMetaData().getInt("hardness") > blade.getHardness(bladeStack))
				{
					if (!this.bladeTooWeak)
					{
						this.bladeTooWeak = true;
					}

					return null;
				} else
				{
					if (this.bladeTooWeak)
					{
						this.bladeTooWeak = false;
					}

					return ret;
				}
			} else
			{
				return null;
			}
		}
	}

	@Override
	public boolean getGuiState(String name)
	{
		return "isBladeTooWeak".equals(name) ? this.bladeTooWeak : super.getGuiState(name);
	}

	@Override
	public Set<UpgradableProperty> getUpgradableProperties()
	{
		return EnumSet.of(
			UpgradableProperty.Processing,
			UpgradableProperty.Transformer,
			UpgradableProperty.EnergyStorage,
			UpgradableProperty.ItemConsuming,
			UpgradableProperty.ItemProducing
		);
	}
}
