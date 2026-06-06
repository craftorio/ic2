package ic2.core.recipe;

import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class MatterAmplifierRecipeManager implements IMachineRecipeManager<IRecipeInput, Integer, ItemStack>
{
	private final List<MachineRecipe<IRecipeInput, Integer>> recipes = new ArrayList<>();

	public boolean addRecipe(IRecipeInput input, Integer output, NBTTagCompound metadata, boolean replace)
	{
		if (output <= 0)
		{
			throw new IllegalArgumentException("non-positive amplification");
		}

		for (ItemStack stack : input.getInputs())
		{
			MachineRecipe<IRecipeInput, Integer> recipe = this.getRecipe(stack, true);
			if (recipe != null)
			{
				if (!replace)
				{
					return false;
				}

				this.recipes.remove(recipe);
			}
		}

		this.recipes.add(new MachineRecipe<>(input, output));
		return true;
	}

	public MachineRecipeResult<IRecipeInput, Integer, ItemStack> apply(ItemStack input, boolean acceptTest)
	{
		MachineRecipe<IRecipeInput, Integer> recipe = this.getRecipe(input, acceptTest);
		return recipe == null ? null : recipe.getResult(StackUtil.copyShrunk(input, recipe.getInput().getAmount()));
	}

	private MachineRecipe<IRecipeInput, Integer> getRecipe(ItemStack stack, boolean acceptTest)
	{
		for (MachineRecipe<IRecipeInput, Integer> recipe : this.recipes)
		{
			if (recipe.getInput().matches(stack) && (acceptTest || recipe.getInput().getAmount() <= StackUtil.getSize(stack)))
			{
				return recipe;
			}
		}

		return null;
	}

	@Override
	public Iterable<? extends MachineRecipe<IRecipeInput, Integer>> getRecipes()
	{
		return this.recipes;
	}

	@Override
	public boolean isIterable()
	{
		return true;
	}
}
