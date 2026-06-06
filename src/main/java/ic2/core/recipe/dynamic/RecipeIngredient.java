package ic2.core.recipe.dynamic;

public abstract class RecipeIngredient<T>
{
	public final T ingredient;

	public RecipeIngredient(T ingredient)
	{
		this.ingredient = ingredient;
	}

	public abstract boolean isEmpty();

	public abstract boolean matches(Object var1);

	public abstract boolean matchesStrict(Object var1);

	public abstract String toStringSafe();
}
