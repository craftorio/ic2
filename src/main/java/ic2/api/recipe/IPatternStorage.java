package ic2.api.recipe;

import java.util.List;

import net.minecraft.world.item.ItemStack;

public interface IPatternStorage
{
	boolean addPattern(ItemStack var1);

	List<ItemStack> getPatterns();
}
