package ic2.core.recipe.dynamic;

import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class RecipeInputItemStack extends RecipeInputIngredient<ItemStack>
{
	public static RecipeInputItemStack of(ItemStack ingredient)
	{
		return new RecipeInputItemStack(ingredient);
	}

	public static RecipeInputItemStack of(ItemStack ingredient, boolean consumable)
	{
		return new RecipeInputItemStack(ingredient, consumable);
	}

	protected RecipeInputItemStack(ItemStack ingredient)
	{
		super(ingredient);
	}

	protected RecipeInputItemStack(ItemStack ingredient, boolean consumable)
	{
		super(ingredient, consumable);
	}

	@Override
	public Object getUnspecific()
	{
		return this.ingredient.getItem();
	}

	@Override
	public RecipeInputIngredient<ItemStack> copy()
	{
		return of(this.ingredient.copy());
	}

	@Override
	public boolean isEmpty()
	{
		return StackUtil.isEmpty(this.ingredient);
	}

	@Override
	public int getCount()
	{
		return StackUtil.getSize(this.ingredient);
	}

	@Override
	public void shrink(int amount)
	{
		this.ingredient.shrink(amount);
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
