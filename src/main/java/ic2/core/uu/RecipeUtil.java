package ic2.core.uu;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

class RecipeUtil
{
	public static List<List<LeanItemStack>> fixIngredientSize(List<ItemStack>[] x)
	{
		List<List<LeanItemStack>> ret = new ArrayList<>(x.length);

		for (int i = 0; i < x.length; i++)
		{
			List<ItemStack> listIn = x[i];
			if (listIn != null)
			{
				List<LeanItemStack> listOut = new ArrayList<>(listIn.size());

				for (ItemStack stack : x[i])
				{
					listOut.add(new LeanItemStack(stack, 1));
				}

				ret.add(listOut);
			}
		}

		return ret;
	}

	public static List<List<LeanItemStack>> convertInputs(List<ItemStack> x)
	{
		return Collections.singletonList(convertOutputs(x));
	}

	public static List<List<LeanItemStack>> convertIngredients(List<Ingredient> x)
	{
		List<List<LeanItemStack>> ret = new ArrayList<>(x.size());

		for (Ingredient ingredient : x)
		{
			ItemStack[] stacks = ingredient.getItems();
			List<LeanItemStack> res = new ArrayList<>(stacks.length);

			for (ItemStack stack : stacks)
			{
				res.add(new LeanItemStack(stack));
			}
		}

		return ret;
	}

	public static List<LeanItemStack> convertOutputs(Collection<ItemStack> x)
	{
		List<LeanItemStack> ret = new ArrayList<>(x.size());

		for (ItemStack stack : x)
		{
			ret.add(new LeanItemStack(stack));
		}

		return ret;
	}
}
