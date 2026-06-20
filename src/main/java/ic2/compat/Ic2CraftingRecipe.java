package ic2.compat;

import net.minecraft.world.item.crafting.CraftingRecipe;


public interface Ic2CraftingRecipe extends CraftingRecipe
{
	int getIc2RecipeWidth();

	int getIc2RecipeHeight();

	default int getHeight()
	{
		return this.getIc2RecipeHeight();
	}

	default int getWidth()
	{
		return this.getIc2RecipeWidth();
	}
}
