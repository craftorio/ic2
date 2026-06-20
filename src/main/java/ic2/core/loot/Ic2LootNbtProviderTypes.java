package ic2.core.loot;

import com.mojang.serialization.MapCodec;
import ic2.core.IC2;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class Ic2LootNbtProviderTypes
{
	public static final LootNbtProviderType BLOCK_NBT = register("block_nbt", Ic2BlockNbtProvider.CODEC);

	public static void init()
	{
	}

	private static LootNbtProviderType register(String id, MapCodec<? extends NbtProvider> codec)
	{
		return Registry.register(BuiltInRegistries.LOOT_NBT_PROVIDER_TYPE, IC2.getIdentifier(id), new LootNbtProviderType(codec));
	}
}
