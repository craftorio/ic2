package ic2.compat;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;


public interface Ic2CraftingRecipe extends CraftingRecipe, ShapedRecipe
{
	int getIc2RecipeWidth();

	int getIc2RecipeHeight();

	@Override
	default int getHeight()
	{
		return this.getIc2RecipeHeight();
	}

	@Override
	default int getWidth()
	{
		return this.getIc2RecipeWidth();
	}
}
