package ic2.core.block.machine.tileentity;

import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.api.recipe.Recipes;
import ic2.api.upgrade.UpgradableProperty;
import ic2.core.IC2;
import ic2.core.block.invslot.InvSlotProcessableGeneric;
import ic2.core.init.IC2Config;
import ic2.core.recipe.BasicListRecipeManager;
import ic2.core.ref.Ic2BlockEntities;
import ic2.core.ref.Ic2Items;
import ic2.core.ref.Ic2SoundEvents;
import ic2.core.util.ConfigUtil;
import ic2.core.util.StackUtil;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileEntityRecycler extends TileEntityStandardMachine<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	public TileEntityRecycler(BlockPos pos, BlockState state)
	{
		super(Ic2BlockEntities.RECYCLER, pos, state, 1, 45, 1);
		this.inputSlot = new InvSlotProcessableGeneric(this, "input", 1, w -> Recipes.recycler);
	}

	public static void init()
	{
		Recipes.recycler = new TileEntityRecycler.RecyclerRecipeManager();
		Recipes.recyclerWhitelist = new BasicListRecipeManager();
		Recipes.recyclerBlacklist = new BasicListRecipeManager();
	}

	public static void initLate()
	{
		try
		{
			for (IRecipeInput input : ConfigUtil.asRecipeInputList(IC2Config.balance.recyclerBlacklist.get()))
			{
				Recipes.recyclerBlacklist.add(input);
			}

			for (IRecipeInput input : ConfigUtil.asRecipeInputList(IC2Config.balance.recyclerWhitelist.get()))
			{
				Recipes.recyclerWhitelist.add(input);
			}
		} catch (ParseException pe)
		{
			throw new RuntimeException(pe);
		}
	}

	public static int recycleChance()
	{
		return 8;
	}

	public static boolean getIsItemBlacklisted(ItemStack aStack)
	{
		return Recipes.recyclerWhitelist.isEmpty() ? Recipes.recyclerBlacklist.contains(aStack) : !Recipes.recyclerWhitelist.contains(aStack);
	}

	@Override
	public SoundEvent getLoopingSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_RECYCLER_OPERATE;
	}

	@Override
	public SoundEvent getInterruptSoundEvent()
	{
		return Ic2SoundEvents.MACHINE_INTERRUPT1;
	}

	@Override
	public void operateOnce(MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> result, Collection<ItemStack> processResult)
	{
		this.inputSlot.consume(result);
		if (IC2.random.nextInt(recycleChance()) == 0)
		{
			this.outputSlot.add(processResult);
		}
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

	private static class RecyclerRecipeManager implements IBasicMachineRecipeManager
	{
		public RecyclerRecipeManager()
		{
		}

		private static Collection<ItemStack> getOutput(ItemStack input)
		{
			return TileEntityRecycler.getIsItemBlacklisted(input) ? Collections.emptyList() : Collections.singletonList(new ItemStack(Ic2Items.SCRAP));
		}

		public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, CompoundTag metadata, boolean replace)
		{
			return false;
		}

		public boolean addRecipe(IRecipeInput input, CompoundTag metadata, boolean replace, ItemStack... outputs)
		{
			return false;
		}

		@Override
		public RecipeOutput getOutputFor(ItemStack input, boolean adjustInput)
		{
			if (StackUtil.isEmpty(input))
			{
				return null;
			}

			RecipeOutput ret = new RecipeOutput(null, new ArrayList<>(getOutput(input)));
			if (adjustInput)
			{
				input.shrink(1);
			}

			return ret;
		}

		public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest)
		{
			return StackUtil.isEmpty(input)
				? null
				: new MachineRecipe<>(Recipes.inputFactory.forStack(input, 1), getOutput(input))
				  .getResult(StackUtil.copyWithSize(input, StackUtil.getSize(input) - 1));
		}

		@Override
		public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isIterable()
		{
			return false;
		}
	}
}
