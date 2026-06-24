package ic2.api.recipe;

import net.minecraft.world.item.ItemStack;

public interface ICraftingRecipeManager
{
	void addRecipe(ItemStack var1, Object... var2);

	void addShapelessRecipe(ItemStack var1, Object... var2);

	record AttributeContainer(boolean hidden, boolean consuming, boolean fixedSize)
		{
			public AttributeContainer(boolean hidden, boolean consuming)
			{
				this(hidden, consuming, false);
			}

		}
}
