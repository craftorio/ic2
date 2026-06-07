package ic2.api.recipe;

import ic2.core.IC2;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;

public class RecipeOutputWeighted
{
	private final List<ItemStack> outputs = new ArrayList<>();
	private final List<Integer> weights = new ArrayList<>();
	private final RandomSource random;

	public RecipeOutputWeighted()
	{
		this.random = IC2.random;
	}

	public RecipeOutputWeighted addOutput(ItemStack output, int weight)
	{
		this.outputs.add(output);
		this.weights.add(weight);
		return this;
	}

	public ItemStack drawOutput()
	{
		if (!this.outputs.isEmpty() && !this.weights.isEmpty())
		{
			int totalWeight = this.weights.stream().mapToInt(Integer::intValue).sum();
			int randomNumber = this.random.nextInt(totalWeight) + 1;
			int cumulativeWeight = 0;

			for (int i = 0; i < this.outputs.size(); i++)
			{
				cumulativeWeight += this.weights.get(i);
				if (randomNumber <= cumulativeWeight)
				{
					return this.outputs.get(i);
				}
			}

			return null;
		} else
		{
			return null;
		}
	}

	public void clear()
	{
		this.outputs.clear();
		this.weights.clear();
	}

	public List<ItemStack> getOutputs()
	{
		return this.outputs;
	}

	public List<Integer> getWeights()
	{
		return this.weights;
	}

	public void forEach(BiConsumer<ItemStack, Integer> consumer)
	{
		for (int i = 0; i < this.outputs.toArray().length; i++)
		{
			ItemStack stack = this.outputs.get(i);
			int weight = this.weights.get(i);
			consumer.accept(stack, weight);
		}
	}
}
