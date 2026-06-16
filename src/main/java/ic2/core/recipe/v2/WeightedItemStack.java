package ic2.core.recipe.v2;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

import java.util.ArrayList;

public class WeightedItemStack
{
	public ItemStack itemStack;
	public int weight;

	public WeightedItemStack(ItemStack itemStack, int weight)
	{
		this.itemStack = itemStack;
		this.weight = weight;
	}

	public static WeightedItemStack of(ItemStack itemStack, int weight)
	{
		return new WeightedItemStack(itemStack, weight);
	}

	public static WeightedItemStack[] of(ItemLike item, int[][] countAndWeights)
	{
		ArrayList<WeightedItemStack> retArr = new ArrayList<>();

		for (int[] countWeight : countAndWeights)
		{
			retArr.add(of(new ItemStack(item, countWeight[0]), countWeight[1]));
		}

		return retArr.toArray(new WeightedItemStack[0]);
	}
}
