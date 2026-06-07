package ic2.core.block.machine;

import ic2.api.recipe.ICannerBottleRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.RecipeOutput;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public class CannerBottleRecipeManager implements ICannerBottleRecipeManager
{
	private final List<MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> recipes = new ArrayList<>();

	public boolean addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output, boolean replace)
	{
		return this.addRecipe(new ICannerBottleRecipeManager.Input(container, fill), output, null, replace);
	}

	@Deprecated
	@Override
	public void addRecipe(IRecipeInput container, IRecipeInput fill, ItemStack output)
	{
		if (!this.addRecipe(container, fill, output, false))
		{
			throw new IllegalStateException("ambiguous canner bottle recipe: " + container + " + " + fill + " -> " + output);
		}
	}

	public boolean addRecipe(ICannerBottleRecipeManager.Input input, ItemStack output, CompoundTag metadata, boolean replace)
	{
		Iterator<MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> it = this.recipes.iterator();

		label35:
		while (it.hasNext())
		{
			MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe = it.next();

			for (ItemStack containerStack : input.container.getInputs())
			{
				for (ItemStack fillStack : input.fill.getInputs())
				{
					if (recipe.getInput().matches(containerStack, fillStack))
					{
						if (!replace)
						{
							IC2.log
								.warn(
									LogCategory.Recipe,
									"ambiguous recipe: ["
										+ input.container.getInputs()
										+ "+"
										+ input.fill.getInputs()
										+ " -> "
										+ output
										+ "], conflicts with ["
										+ recipe.getInput().container.getInputs()
										+ "+"
										+ recipe.getInput().fill.getInputs()
										+ " -> "
										+ recipe.getOutput()
										+ "]"
								);
							return false;
						}

						it.remove();
						continue label35;
					}
				}
			}
		}

		this.recipes.add(new MachineRecipe<>(input, output));
		return true;
	}

	@Override
	public RecipeOutput getOutputFor(ItemStack container, ItemStack fill, boolean adjustInput, boolean acceptTest)
	{
		if (acceptTest)
		{
			if (StackUtil.isEmpty(container) && StackUtil.isEmpty(fill))
			{
				return null;
			}
		} else if (StackUtil.isEmpty(container) || StackUtil.isEmpty(fill))
		{
			return null;
		}

		for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : this.recipes)
		{
			ICannerBottleRecipeManager.Input recipeInput = recipe.getInput();
			if (acceptTest && StackUtil.isEmpty(container))
			{
				if (recipeInput.fill.matches(fill))
				{
					return new RecipeOutput(null, recipe.getOutput());
				}
			} else if (acceptTest && StackUtil.isEmpty(fill))
			{
				if (recipeInput.container.matches(container))
				{
					return new RecipeOutput(null, recipe.getOutput());
				}
			} else if (recipeInput.matches(container, fill))
			{
				if (!acceptTest
					&& (
					StackUtil.isEmpty(container)
						|| StackUtil.getSize(container) < recipeInput.container.getAmount()
						|| StackUtil.getSize(fill) < recipeInput.fill.getAmount()
				))
				{
					break;
				}

				if (adjustInput)
				{
					if (!StackUtil.isEmpty(container))
					{
						container.m_41774_(recipeInput.container.getAmount());
					}

					fill.m_41774_(recipeInput.fill.getAmount());
				}

				new RecipeOutput(null, recipe.getOutput());
				break;
			}
		}

		return null;
	}

	public MachineRecipeResult<ICannerBottleRecipeManager.Input, ItemStack, ICannerBottleRecipeManager.RawInput> apply(
		ICannerBottleRecipeManager.RawInput input, boolean acceptTest
	)
	{
		boolean emptyContainer = StackUtil.isEmpty(input.container);
		boolean emptyFill = StackUtil.isEmpty(input.fill);
		if (acceptTest || !emptyContainer && !emptyFill)
		{
			if (acceptTest && emptyContainer && emptyFill)
			{
				return null;
			}

			for (MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack> recipe : this.recipes)
			{
				if ((
					emptyContainer
						|| recipe.getInput().container.matches(input.container) && recipe.getInput().container.getAmount() <= StackUtil.getSize(input.container)
				)
					&& (emptyFill || recipe.getInput().fill.matches(input.fill) && recipe.getInput().fill.getAmount() <= StackUtil.getSize(input.fill)))
				{
					return recipe.getResult(
						new ICannerBottleRecipeManager.RawInput(
							emptyContainer ? StackUtil.emptyStack : StackUtil.copyShrunk(input.container, recipe.getInput().container.getAmount()),
							emptyFill ? StackUtil.emptyStack : StackUtil.copyShrunk(input.fill, recipe.getInput().fill.getAmount())
						)
					);
				}
			}

			return null;
		} else
		{
			return null;
		}
	}

	@Override
	public Iterable<? extends MachineRecipe<ICannerBottleRecipeManager.Input, ItemStack>> getRecipes()
	{
		return this.recipes;
	}

	@Override
	public boolean isIterable()
	{
		return true;
	}
}
