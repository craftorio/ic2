package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class Ic2EntityTags
{
	public static final TagKey<EntityType<?>> SCAFFOLD_CLIMBABLE = create("scaffold_climbable");

	public static void init()
	{
	}

	private static TagKey<EntityType<?>> create(String name)
	{
		return TagKey.create(Registries.ENTITY_TYPE, IC2.getIdentifier(name));
	}

	private static TagKey<EntityType<?>> create(String fabricName, String forgeName)
	{
		ResourceLocation id = ResourceLocation.parse(IC2.envProxy.isFabricEnv() ? fabricName : forgeName);
		return TagKey.create(Registries.ENTITY_TYPE, id);
	}
}
