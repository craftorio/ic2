package ic2.core.recipe.input;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import ic2.api.recipe.IRecipeInput;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

public class RecipeInputMultiple extends RecipeInputBase
{
	public final IRecipeInput[] inputs;
	private final int amount;

	public RecipeInputMultiple(int amount, IRecipeInput... inputs)
	{
		this.inputs = inputs;
		this.amount = amount;
	}

	public RecipeInputMultiple(int amount, List<IRecipeInput> inputs)
	{
		this.inputs = inputs.toArray(new IRecipeInput[0]);
		this.amount = amount;
	}

	@Override
	public boolean matches(ItemStack subject)
	{
		for (IRecipeInput input : this.inputs)
		{
			if (input.matches(subject))
			{
				return true;
			}
		}

		return false;
	}

	@Override
	public int getAmount()
	{
		return this.amount;
	}

	@Override
	public List<ItemStack> listStacks()
	{
		List<ItemStack> list = new ArrayList<>();

		for (IRecipeInput input : this.inputs)
		{
			list.addAll(input.getInputs());
		}

		return list;
	}

	@Override
	public String toString()
	{
		if (this.inputs.length <= 0)
		{
			return "RecipeInputMultiple<Nothing>";
		}

		StringBuilder b = new StringBuilder("RecipeInputMultiple<");
		int i = 0;
		int end = this.inputs.length - 1;

		while (true)
		{
			b.append(this.inputs[i].toString());
			if (i == end)
			{
				return b.append('>').toString();
			}

			b.append(", ");
			i++;
		}
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj != null && this.getClass() == obj.getClass())
		{
			IRecipeInput[] otherInputs = ((RecipeInputMultiple) obj).inputs;
			if (this.inputs.length == otherInputs.length)
			{
				for (int i = 0; i < this.inputs.length; i++)
				{
					if (!this.inputs[i].equals(otherInputs[i]))
					{
						return false;
					}
				}

				return true;
			}
		}

		return false;
	}

	@Override
	public JsonElement toJson()
	{
		if (this.inputs.length == 1)
		{
			return ((RecipeInputBase) this.inputs[0]).toJson();
		}

		JsonArray array = new JsonArray();

		for (IRecipeInput input : this.inputs)
		{
			array.add(((RecipeInputBase) input).toJson());
		}

		return array;
	}
}
