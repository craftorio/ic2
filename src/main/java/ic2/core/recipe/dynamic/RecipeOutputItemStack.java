package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeOutputItemStack extends RecipeOutputIngredient<ItemStack>
{
	public static RecipeOutputItemStack of(ItemStack ingredient)
	{
		return new RecipeOutputItemStack(ingredient);
	}

	protected RecipeOutputItemStack(ItemStack ingredient)
	{
		super(ingredient);
	}

	@Override
	public RecipeOutputIngredient<ItemStack> copy()
	{
		return of(this.ingredient.copy());
	}

	@Override
	public boolean isEmpty()
	{
		return StackUtil.isEmpty(this.ingredient);
	}

	@Override
	public boolean matches(Object other)
	{
		return !(other instanceof ItemStack) ? false : StackUtil.checkItemEqualityStrict(this.ingredient, (ItemStack) other);
	}

	@Override
	public boolean matchesStrict(Object other)
	{
		return this.matches(other);
	}

	@Override
	public String toStringSafe()
	{
		return StackUtil.toStringSafe(this.ingredient);
	}
}
