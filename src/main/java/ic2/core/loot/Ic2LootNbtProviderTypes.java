package ic2.core.loot;

import ic2.core.IC2;
import net.minecraft.core.Registry;
import net.minecraft.world.level.storage.loot.Serializer;
import net.minecraft.world.level.storage.loot.providers.nbt.LootNbtProviderType;
import net.minecraft.world.level.storage.loot.providers.nbt.NbtProvider;

public class Ic2LootNbtProviderTypes
{
	public static final LootNbtProviderType BLOCK_NBT = register("block_nbt", new Ic2BlockNbtProvider.Serializer());

	private static LootNbtProviderType register(String id, Serializer<? extends NbtProvider> jsonSerializer)
	{
		return (LootNbtProviderType) Registry.register(Registry.LOOT_NBT_PROVIDER_TYPE, IC2.getIdentifier(id), new LootNbtProviderType(jsonSerializer));
	}
}
