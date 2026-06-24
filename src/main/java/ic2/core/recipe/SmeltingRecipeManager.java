package ic2.core.recipe;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.IC2;
import ic2.core.util.StackUtil;

import java.util.Collection;
import java.util.Collections;

import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;

public class SmeltingRecipeManager implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack>
{
	public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(ItemStack input, boolean acceptTest)
	{
		SmeltingRecipe recipe = (SmeltingRecipe) IC2.sideProxy
			.getRecipeManager()
			.getRecipeFor(RecipeType.SMELTING, new SimpleContainer(new ItemStack[] { input }), null)
			.orElse(null);
		if (recipe == null)
		{
			return null;
		}

		ItemStack output = recipe.getResultItem((RegistryAccess) null);
		if (StackUtil.isEmpty(output))
		{
			return null;
		}

		CompoundTag nbt = new CompoundTag();
		nbt.putFloat("experience", recipe.getExperience() * StackUtil.getSize(output));
		return new MachineRecipe<>(input, output, nbt).getResult(StackUtil.copyShrunk(input, 1));
	}

	@Override
	public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes()
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isIterable()
	{
		return false;
	}

	public enum SmeltingBridge implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
	{
		INSTANCE;

		public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest)
		{
			MachineRecipeResult<ItemStack, ItemStack, ItemStack> normal = Recipes.furnace.apply(input, acceptTest);
			if (normal == null)
			{
				return null;
			}

			MachineRecipe<ItemStack, ItemStack> result = normal.recipe();
			IRecipeInput resultIn = Recipes.inputFactory.forStack(result.getInput());
			Collection<ItemStack> resultOut = Collections.singletonList(result.getOutput());
			CompoundTag resultNBT = result.getMetaData();
			return new MachineRecipe<>(resultIn, resultOut, resultNBT).getResult(normal.adjustedInput());
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
