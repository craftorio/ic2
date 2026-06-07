package ic2.core.recipe;

import ic2.api.recipe.IListRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class BasicListRecipeManager extends MachineRecipeHelper<IRecipeInput, Object> implements IListRecipeManager
{
	private static final Object dummyOutput = new Object();

	@Override
	public void add(IRecipeInput input)
	{
		if (input == null)
		{
			throw new NullPointerException("Input must not be null.");
		}

		this.addRecipe(input, dummyOutput, null, false);
	}

	@Override
	public boolean contains(ItemStack stack)
	{
		return StackUtil.isEmpty(stack) ? false : this.getRecipe(stack) != null;
	}

	@Override
	public boolean isEmpty()
	{
		return this.recipes.isEmpty();
	}

	@Override
	public List<IRecipeInput> getInputs()
	{
		return new ArrayList<>(this.recipes.keySet());
	}

	@Override
	public Iterator<IRecipeInput> iterator()
	{
		return this.recipes.keySet().iterator();
	}

	public boolean addRecipe(IRecipeInput input, Object output, CompoundTag metadata, boolean replace)
	{
		for (ItemStack is : input.getInputs())
		{
			MachineRecipe<IRecipeInput, Object> recipe = this.getRecipe(is);
			if (recipe != null)
			{
				if (!replace)
				{
					IC2.log.debug(LogCategory.Recipe, "Skipping %s due to duplicate recipe for %s (%s)", input, is, recipe.getInput());
					return false;
				}

				while (true)
				{
					this.recipes.remove(input);
					this.removeCachedRecipes(input);
					recipe = this.getRecipe(is);
					if (recipe == null)
					{
						break;
					}
				}
			}
		}

		MachineRecipe<IRecipeInput, Object> recipe = new MachineRecipe<>(input, output, metadata);
		this.recipes.put(input, recipe);
		this.addToCache(recipe);
		return false;
	}

	protected IRecipeInput getForInput(IRecipeInput input)
	{
		return input;
	}

	@Override
	protected boolean consumeContainer(ItemStack input, ItemStack container, MachineRecipe<IRecipeInput, Object> recipe)
	{
		return true;
	}
}
