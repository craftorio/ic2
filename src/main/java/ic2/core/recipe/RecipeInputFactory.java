package ic2.core.recipe;

import com.google.common.collect.Iterables;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IRecipeInputFactory;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;

public class RecipeInputFactory implements IRecipeInputFactory
{
	@Override
	public IRecipeInput forStack(ItemStack stack)
	{
		return new RecipeInputItemStack(stack);
	}

	@Override
	public IRecipeInput forStack(ItemStack stack, int amount)
	{
		return new RecipeInputItemStack(stack, amount);
	}

	@Override
	public IRecipeInput forExactStack(ItemStack stack)
	{
		return stack.getMetadata() == 32767 ? this.forStack(stack) : new RecipeInputItemStackExact(stack);
	}

	@Override
	public IRecipeInput forExactStack(ItemStack stack, int amount)
	{
		return stack.getMetadata() == 32767 ? this.forStack(stack, amount) : new RecipeInputItemStackExact(stack, amount);
	}

	@Override
	public IRecipeInput forOreDict(String name)
	{
		return new RecipeInputOreDict(name);
	}

	@Override
	public IRecipeInput forOreDict(String name, int amount)
	{
		return new RecipeInputOreDict(name, amount);
	}

	@Override
	public IRecipeInput forOreDict(String name, int amount, int metaOverride)
	{
		return new RecipeInputOreDict(name, amount, metaOverride);
	}

	@Override
	public IRecipeInput forFluidContainer(Fluid fluid)
	{
		return new RecipeInputFluidContainer(fluid);
	}

	@Override
	public IRecipeInput forFluidContainer(Fluid fluid, int amount)
	{
		return new RecipeInputFluidContainer(fluid, amount);
	}

	@Override
	public IRecipeInput forAny(IRecipeInput... options)
	{
		return new RecipeInputMultiple(options);
	}

	@Override
	public IRecipeInput forAny(Iterable<IRecipeInput> options)
	{
		return options instanceof Collection
			? new RecipeInputMultiple((IRecipeInput[]) ((Collection) options).toArray(new IRecipeInput[0]))
			: new RecipeInputMultiple((IRecipeInput[]) Iterables.toArray(options, IRecipeInput.class));
	}

	@Override
	public Ingredient getIngredient(IRecipeInput input)
	{
		return new IngredientRecipeInput(input);
	}

	@Override
	public IRecipeInput forIngredient(Ingredient ingredient)
	{
		return new RecipeInputIngredient(ingredient);
	}
}
