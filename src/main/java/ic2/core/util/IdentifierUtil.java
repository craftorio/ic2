package ic2.core.util;

import net.minecraft.core.Registry;
import net.minecraft.world.level.ItemLike;

public class IdentifierUtil
{
	public static String getPath(ItemLike item)
	{
		return Registry.f_122827_.getKey(item.m_5456_()).m_135815_();
	}

	public static String getNamespace(ItemLike item)
	{
		return Registry.f_122827_.getKey(item.m_5456_()).m_135827_();
	}
}
