package ic2.core.init;

import ic2.core.util.ItemComparableItemStack;
import ic2.core.util.StackUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.world.item.ItemStack;

public class OreValues
{
	private static final Map<ItemComparableItemStack, Integer> stackValues = new HashMap<>();

	public static void add(ItemStack stack, int value)
	{
		if (value <= 0)
		{
			throw new IllegalArgumentException("value has to be > 0");
		}

		ItemComparableItemStack key = new ItemComparableItemStack(stack, true);
		Integer prev = stackValues.put(key, value);
		if (prev != null && prev > value)
		{
			stackValues.put(key, prev);
		}
	}

	public static int get(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return 0;
		}

		Integer ret = stackValues.get(new ItemComparableItemStack(stack, false));
		return ret != null ? ret * StackUtil.getSize(stack) : 0;
	}

	public static int get(List<ItemStack> stacks)
	{
		int ret = 0;

		for (ItemStack stack : stacks)
		{
			ret += get(stack);
		}

		return ret;
	}
}
