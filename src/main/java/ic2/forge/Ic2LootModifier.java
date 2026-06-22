package ic2.forge;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class Ic2LootModifier extends LootModifier
{
	public static final Codec<Ic2LootModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance).apply(instance, Ic2LootModifier::new));
	public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> lootModifiersRegistry = DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "ic2");
	private static final Map<ResourceLocation, ResourceLocation> AllLootTables = Map.ofEntries(
		Map.entry(BuiltInLootTables.ABANDONED_MINESHAFT, ResourceLocation.fromNamespaceAndPath("ic2", "chests/abandoned_mineshaft")),
		Map.entry(BuiltInLootTables.DESERT_PYRAMID, ResourceLocation.fromNamespaceAndPath("ic2", "chests/desert_pyramid")),
		Map.entry(BuiltInLootTables.END_CITY_TREASURE, ResourceLocation.fromNamespaceAndPath("ic2", "chests/end_city_treasure")),
		Map.entry(BuiltInLootTables.IGLOO_CHEST, ResourceLocation.fromNamespaceAndPath("ic2", "chests/igloo_chest")),
		Map.entry(BuiltInLootTables.JUNGLE_TEMPLE, ResourceLocation.fromNamespaceAndPath("ic2", "chests/jungle_temple")),
		Map.entry(BuiltInLootTables.NETHER_BRIDGE, ResourceLocation.fromNamespaceAndPath("ic2", "chests/nether_bridge")),
		Map.entry(BuiltInLootTables.SIMPLE_DUNGEON, ResourceLocation.fromNamespaceAndPath("ic2", "chests/simple_dungeon")),
		Map.entry(BuiltInLootTables.SPAWN_BONUS_CHEST, ResourceLocation.fromNamespaceAndPath("ic2", "chests/spawn_bonus_chest")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CORRIDOR, ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_corridor")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CROSSING, ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_crossing")),
		Map.entry(BuiltInLootTables.STRONGHOLD_LIBRARY, ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_library")),
		Map.entry(BuiltInLootTables.VILLAGE_TOOLSMITH, ResourceLocation.fromNamespaceAndPath("ic2", "chests/village_toolsmith"))
	);

	static
	{
		lootModifiersRegistry.register("inject", () -> CODEC);
	}

	public Ic2LootModifier(LootItemCondition[] conditionsIn)
	{
		super(conditionsIn);
	}

	@Override
	protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context)
	{
		ResourceLocation iLootId = AllLootTables.get(context.getQueriedLootTableId());
		if (iLootId == null)
		{
			return generatedLoot;
		}
		LootTable table = context.getLevel().getServer().getLootData().getLootTable(iLootId);
		LootParams params = new LootParams.Builder(context.getLevel())
			.withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
			.withOptionalParameter(LootContextParams.THIS_ENTITY, context.getParamOrNull(LootContextParams.THIS_ENTITY))
			.withLuck(context.getLuck())
			.create(LootContextParamSets.CHEST);
		generatedLoot.addAll(table.getRandomItems(params));
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec()
	{
		return CODEC;
	}
}
