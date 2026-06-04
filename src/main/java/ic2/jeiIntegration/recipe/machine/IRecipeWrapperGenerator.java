package ic2.jeiIntegration.recipe.machine;

import ic2.api.item.IBlockCuttingBlade;
import ic2.api.recipe.IBasicMachineRecipeManager;
import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.ICannerEnrichRecipeManager;
import ic2.api.recipe.IElectrolyzerRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.item.type.BlockCuttingBladeType;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.BlockName;
import ic2.core.ref.ItemName;
import ic2.core.ref.TeBlock;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import mezz.jei.api.recipe.IRecipeWrapper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

public interface IRecipeWrapperGenerator<T>
{
	IRecipeWrapperGenerator<IBasicMachineRecipeManager> basicMachine = category ->
	{
		ArrayList<IRecipeWrapper> recipes = new ArrayList<>();
		for (MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe : category.recipeManager.getRecipes())
		{
			recipes.add(new IORecipeWrapper(machineRecipe, category));
		}
		return recipes;
	};
	IRecipeWrapperGenerator<IBasicMachineRecipeManager> recycler = category ->
	{
		IRecipeInput input = new IRecipeInput()
		{

			@Override
			public boolean matches(ItemStack subject)
			{
				return StackUtil.checkItemEquality(subject, BlockName.te.getItemStack(TeBlock.recycler));
			}

			@Override
			public List<ItemStack> getInputs()
			{
				return Collections.singletonList(BlockName.te.getItemStack(TeBlock.recycler));
			}

			@Override
			public int getAmount()
			{
				return 1;
			}
		};
		return Collections.singletonList(new IORecipeWrapper(new MachineRecipe<>(input, Collections.singletonList(ItemName.crafting.getItemStack(CraftingItemType.scrap))), category));
	};
	IRecipeWrapperGenerator<IBasicMachineRecipeManager> blockCutter = new IRecipeWrapperGenerator<IBasicMachineRecipeManager>()
	{
		private final List<ItemStack> candidates = Arrays.asList(ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.iron), ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.steel), ItemName.block_cutting_blade.getItemStack(BlockCuttingBladeType.diamond));

		@Override
		public List<IRecipeWrapper> getRecipeList(IORecipeCategory<IBasicMachineRecipeManager> category)
		{
			ArrayList<IRecipeWrapper> list = new ArrayList<>();
			for (MachineRecipe<IRecipeInput, Collection<ItemStack>> machineRecipe : category.recipeManager.getRecipes())
			{
				list.add(new AdvancedIORecipeWrapper(machineRecipe, this.getInput(this.getHardness(machineRecipe.getMetaData())), category));
			}
			return list;
		}

		private int getHardness(NBTTagCompound metadata)
		{
			if (metadata == null)
			{
				return Integer.MAX_VALUE;
			}
			return metadata.getInteger("hardness");
		}

		private IRecipeInput getInput(final int hardness)
		{
			return new IRecipeInput()
			{
				@Override
				public boolean matches(ItemStack subject)
				{
					return subject != null && subject.getItem() instanceof IBlockCuttingBlade && ((IBlockCuttingBlade) subject.getItem()).getHardness(subject) > hardness;
				}

				@Override
				public List<ItemStack> getInputs()
				{
					ArrayList<ItemStack> list = new ArrayList<>(candidates.size());
					for (ItemStack stack : candidates)
					{
						if (((IBlockCuttingBlade) stack.getItem()).getHardness(stack) < hardness) continue;
						list.add(stack);
					}
					return list;
				}

				@Override
				public int getAmount()
				{
					return 1;
				}
			};
		}
	};
	IRecipeWrapperGenerator<IElectrolyzerRecipeManager> electrolyzer = category ->
	{
		ArrayList<IRecipeWrapper> recipes = new ArrayList<>();
		for (Map.Entry<String, IElectrolyzerRecipeManager.ElectrolyzerRecipe> recipe : category.recipeManager.getRecipeMap().entrySet())
		{
			Fluid input = FluidRegistry.getFluid(recipe.getKey());
			if (input == null) continue;
			recipes.add(new ElectrolyzerWrapper(new FluidStack(input, recipe.getValue().inputAmount), recipe.getValue().outputs, category));
		}
		return recipes;
	};
	IRecipeWrapperGenerator<ICannerEnrichRecipeManager> cannerEnrichment = category ->
	{
		ArrayList<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
		for (MachineRecipe recipe : category.recipeManager.getRecipes())
		{
			recipes.add(new CannerEnrichmentWrapper((ICannerEnrichRecipeManager.Input) recipe.getInput(), (FluidStack) recipe.getOutput(), category));
		}
		return recipes;
	};
	IRecipeWrapperGenerator<ICannerBottleRecipeManager> cannerBottling = category ->
	{
		ArrayList<IRecipeWrapper> recipes = new ArrayList<IRecipeWrapper>();
		for (MachineRecipe recipe : category.recipeManager.getRecipes())
		{
			recipes.add(new CannerCanningWrapper((ICannerBottleRecipeManager.Input) recipe.getInput(), (ItemStack) recipe.getOutput(), category));
		}
		return recipes;
	};

	List<IRecipeWrapper> getRecipeList(IORecipeCategory<T> var1);
}