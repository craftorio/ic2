package ic2.core.recipe.input;

import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.IRecipeInputFactory;
import ic2.core.util.StackUtil;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

public class RecipeInputFactory implements IRecipeInputFactory
{
	@Override
	public IRecipeInput forItem(ItemLike item)
	{
		return new RecipeInputItemStack(new ItemStack(item));
	}

	@Override
	public IRecipeInput forStack(ItemStack stack)
	{
		return new RecipeInputItemStack(stack);
	}

	@Override
	public IRecipeInput forStack(ItemStack stack, int amount)
	{
		return new RecipeInputItemStack(StackUtil.copyWithSize(stack, amount));
	}

	@Override
	public IRecipeInput forTag(String name, int amount)
	{
		return this.forIngredient(Ingredient.m_204132_(TagKey.m_203882_(Registry.f_122904_, ResourceLocation.fromNamespaceAndPath(name))), amount);
	}

	@Override
	public IRecipeInput forFluidContainer(Fluid fluid, int amount)
	{
		return new RecipeInputFluidContainer(fluid, amount);
	}

	@Override
	public Ingredient getIngredient(IRecipeInput input)
	{
		return input.getIngredient();
	}

	@Override
	public IRecipeInput forIngredient(Ingredient ingredient, int amount)
	{
		return new RecipeInputIngredient(ingredient, amount);
	}
}
