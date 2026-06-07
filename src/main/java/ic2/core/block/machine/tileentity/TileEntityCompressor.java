package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.LiquidUtil;
import ic2.core.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

public class TileEntityCompressor extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	protected boolean usingPumpRecipe;
	protected final Set<TileEntityPump> pumps = new HashSet<>(12, 0.5F);

	public TileEntityCompressor(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.COMPRESSOR, pos, state, 2, 300, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, Recipes.compressor);
	}

	@Override
	protected void onLoaded()
	{
		super.onLoaded();
		this.findPumps();
	}

	@Override
	protected void onNeighborChange(Block neighbor, BlockPos neighborPos)
	{
		super.onNeighborChange(neighbor, neighborPos);
		this.findPumps();
	}

	protected void findPumps()
	{
		Level world = this.getLevel();
		this.pumps.clear();

		for (Direction side : Util.ALL_DIRS)
		{
			BlockEntity te = world.getBlockEntity(this.worldPosition.relative(side));
			if (te instanceof TileEntityPump)
			{
				this.pumps.add((TileEntityPump) te);
			}
		}
	}

	@Override
	public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> getRecipeResult()
	{
		this.usingPumpRecipe = false;
		MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output = super.getRecipeResult();
		if (output != null)
		{
			return output;
		}

		if (!this.pumps.isEmpty() && this.inputSlot.isEmpty() && this.outputSlot.canAdd(new ItemStack(Items.f_42452_)))
		{
			int reqAmount = 1000;

			for (TileEntityPump pump : this.pumps)
			{
				int amount = LiquidUtil.drainTile(pump, Direction.UP, Fluids.f_76193_, reqAmount, true);
				if (amount > 0)
				{
					reqAmount -= amount;
					if (reqAmount <= 0)
					{
						this.usingPumpRecipe = true;
						output = new MachineRecipe<>(null, Collections.singletonList(new ItemStack(Items.f_42452_))).getResult(null);
						break;
					}
				}
			}
		}

		return output;
	}

	@Override
	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> output, Collection<ItemStack> processResult)
	{
		if (this.usingPumpRecipe)
		{
			int reqAmount = 1000;

			for (TileEntityPump pump : this.pumps)
			{
				int amount = LiquidUtil.drainTile(pump, Direction.UP, Fluids.f_76193_, reqAmount, false);
				if (amount > 0)
				{
					reqAmount -= amount;
					if (reqAmount <= 0)
					{
						break;
					}
				}
			}

			assert reqAmount == 0;
			this.outputSlot.add(processResult);
		} else
		{
			super.operateOnce(output, processResult);
		}
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_COMPRESSOR_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
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
