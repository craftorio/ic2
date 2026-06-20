package ic2.forge;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import ic2.core.IC2;
import ic2.core.util.LogCategory;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class Ic2LootModifier extends LootModifier
{
	private static final Map<ResourceLocation, ResourceLocation> AllLootTables = Map.ofEntries(
		Map.entry(BuiltInLootTables.ABANDONED_MINESHAFT.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/abandoned_mineshaft")),
		Map.entry(BuiltInLootTables.DESERT_PYRAMID.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/desert_pyramid")),
		Map.entry(BuiltInLootTables.END_CITY_TREASURE.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/end_city_treasure")),
		Map.entry(BuiltInLootTables.IGLOO_CHEST.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/igloo_chest")),
		Map.entry(BuiltInLootTables.JUNGLE_TEMPLE.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/jungle_temple")),
		Map.entry(BuiltInLootTables.NETHER_BRIDGE.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/nether_bridge")),
		Map.entry(BuiltInLootTables.SIMPLE_DUNGEON.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/simple_dungeon")),
		Map.entry(BuiltInLootTables.SPAWN_BONUS_CHEST.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/spawn_bonus_chest")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CORRIDOR.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_corridor")),
		Map.entry(BuiltInLootTables.STRONGHOLD_CROSSING.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_crossing")),
		Map.entry(BuiltInLootTables.STRONGHOLD_LIBRARY.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/stronghold_library")),
		Map.entry(BuiltInLootTables.VILLAGE_TOOLSMITH.location(), ResourceLocation.fromNamespaceAndPath("ic2", "chests/village_toolsmith"))
	);

	public static final MapCodec<Ic2LootModifier> CODEC = RecordCodecBuilder.mapCodec(instance ->
		codecStart(instance).apply(instance, Ic2LootModifier::new));
	
	public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> lootModifiersRegistry = DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "ic2");
	
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
		LootTable table = context.getLevel().getServer().reloadableRegistries().getLootTable(ResourceKey.create(net.minecraft.core.registries.Registries.LOOT_TABLE, iLootId));
		LootParams params = new LootParams.Builder(context.getLevel())
			.withParameter(LootContextParams.ORIGIN, context.getParam(LootContextParams.ORIGIN))
			.withOptionalParameter(LootContextParams.THIS_ENTITY, context.getParamOrNull(LootContextParams.THIS_ENTITY))
			.withLuck(context.getLuck())
			.create(LootContextParamSets.CHEST);
		generatedLoot.addAll(table.getRandomItems(params));
		return generatedLoot;
	}

	@Override
	public MapCodec<? extends IGlobalLootModifier> codec()
	{
		return CODEC;
	}
}
