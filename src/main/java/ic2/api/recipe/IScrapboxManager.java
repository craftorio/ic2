package ic2.api.recipe;

import java.util.Map;

import net.minecraft.world.item.ItemStack;

public interface IScrapboxManager extends IBasicMachineRecipeManager
{
	ItemStack getDrop(ItemStack var1, boolean var2);

	Map<ItemStack, Float> getDrops();
}
