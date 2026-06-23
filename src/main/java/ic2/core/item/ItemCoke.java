package ic2.core.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemCoke extends Item
{
	public ItemCoke()
	{
		super(new Properties().stacksTo(64));
	}

	@Override
	public int getBurnTime(ItemStack itemStack, net.minecraft.world.item.crafting.RecipeType<?> recipeType)
	{
		return 3200;
	}
}
