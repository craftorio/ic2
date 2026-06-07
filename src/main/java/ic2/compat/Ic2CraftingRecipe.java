package ic2.compat;

import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraftforge.common.crafting.IShapedRecipe;

public interface Ic2CraftingRecipe extends CraftingRecipe, IShapedRecipe<CraftingContainer>
{
	int getIc2RecipeWidth();

	int getIc2RecipeHeight();

	@Override
	default int getRecipeHeight()
	{
		return this.getIc2RecipeHeight();
	}

	@Override
	default int getRecipeWidth()
	{
		return this.getIc2RecipeWidth();
	}
}
