package ic2.core.recipe.v2;

import ic2.api.recipe.Recipes;

import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Function;

import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;

public class RecipeManagerGetter<T> implements Recipes.IGetter<T>
{
	private final Function<RecipeManager, T> factory;
	private final Map<RecipeManager, T> cache = new WeakHashMap<>();

	public RecipeManagerGetter(Function<RecipeManager, T> factory)
	{
		this.factory = factory;
	}

	@Override
	public T get(Level world)
	{
		return world.m_5776_() ? this.factory.apply(world.m_7465_()) : this.cache.computeIfAbsent(world.m_7465_(), this.factory);
	}
}
