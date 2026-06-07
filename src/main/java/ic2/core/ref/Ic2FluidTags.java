package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

public final class Ic2FluidTags
{
	public static final TagKey<Fluid> STEAM = create("c:steam", "forge:steam");

	public static void init()
	{
	}

	private static TagKey<Fluid> create(String fabricName, String forgeName)
	{
		ResourceLocation id = ResourceLocation.fromNamespaceAndPath(IC2.envProxy.isFabricEnv() ? fabricName : forgeName);
		return TagKey.m_203882_(Registry.f_122899_, id);
	}
}
