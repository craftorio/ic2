package ic2.core.util;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.ItemLike;

public class IdentifierUtil
{
	public static String getPath(ItemLike item)
	{
		return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
	}

	public static String getNamespace(ItemLike item)
	{
		return BuiltInRegistries.ITEM.getKey(item.asItem()).getNamespace();
	}
}
