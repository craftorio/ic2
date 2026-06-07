package ic2.api.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

@Deprecated(forRemoval = true)
public interface IRecipeInputFactory
{
	IRecipeInput forItem(ItemLike var1);

	IRecipeInput forStack(ItemStack var1);

	IRecipeInput forStack(ItemStack var1, int var2);

	IRecipeInput forTag(String var1, int var2);

	IRecipeInput forFluidContainer(Fluid var1, int var2);

	IRecipeInput forIngredient(Ingredient var1, int var2);

	@Deprecated
	Ingredient getIngredient(IRecipeInput var1);
}
