package ic2.core.ref;

import ic2.core.IC2;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class Ic2ItemTags
{
	public static final TagKey<Item> BRONZE_INGOTS = createResource("bronze", "ingots");
	public static final TagKey<Item> LEAD_INGOTS = createResource("lead", "ingots");
	public static final TagKey<Item> PLUTONIUM_INGOTS = createResource("plutonium", "ingots");
	public static final TagKey<Item> SILVER_INGOTS = createResource("silver", "ingots");
	public static final TagKey<Item> STEEL_INGOTS = createResource("steel", "ingots");
	public static final TagKey<Item> TIN_INGOTS = createResource("tin", "ingots");
	public static final TagKey<Item> URANIUM_INGOTS = createResource("uranium", "ingots");
	public static final TagKey<Item> IRIDIUM_NUGGETS = createResource("iridium", "nuggets");
	public static final TagKey<Item> COAL_DUSTS = createResource("coal", "dusts");
	public static final TagKey<Item> COPPER_DUSTS = createResource("copper", "dusts");
	public static final TagKey<Item> DIAMOND_DUSTS = createResource("diamond", "dusts");
	public static final TagKey<Item> GOLD_DUSTS = createResource("gold", "dusts");
	public static final TagKey<Item> IRON_DUSTS = createResource("iron", "dusts");
	public static final TagKey<Item> LAPIS_DUSTS = createResource("lapis", "dusts");
	public static final TagKey<Item> LEAD_DUSTS = createResource("lead", "dusts");
	public static final TagKey<Item> OBSIDIAN_DUSTS = createResource("obsidian", "dusts");
	public static final TagKey<Item> SILVER_DUSTS = createResource("silver", "dusts");
	public static final TagKey<Item> STONE_DUSTS = createResource("stone", "dusts");
	public static final TagKey<Item> SULFUR_DUSTS = createResource("sulfur", "dusts");
	public static final TagKey<Item> TIN_DUSTS = createResource("tin", "dusts");
	public static final TagKey<Item> IRON_PLATES = createResource("iron", "plates");
	public static final TagKey<Item> GOLD_PLATES = createResource("gold", "plates");
	public static final TagKey<Item> LEAD_PLATES = createResource("lead", "plates");
	public static final TagKey<Item> BRONZE_PLATES = createResource("bronze", "plates");
	public static final TagKey<Item> TIN_PLATES = createResource("tin", "plates");
	public static final TagKey<Item> COPPER_PLATES = createResource("copper", "plates");
	public static final TagKey<Item> LAPIS_PLATES = createResource("lapis", "plates");
	public static final TagKey<Item> OBSIDIAN_PLATES = createResource("obsidian", "plates");
	public static final TagKey<Item> STEEL_PLATES = createResource("steel", "plates");
	public static final TagKey<Item> ORES = create("c:ores", "forge:ores");
	public static final TagKey<Item> LEAD_ORES = createResource("lead", "ores");
	public static final TagKey<Item> LEAD_RAW_ORES = createResource("lead", "raw_ores");
	public static final TagKey<Item> SILVER_ORES = createResource("silver", "ores");
	public static final TagKey<Item> TIN_RAW_ORES = createResource("tin", "raw_ores");
	public static final TagKey<Item> TIN_ORES = createResource("tin", "ores");
	public static final TagKey<Item> URANIUM_ORES = createResource("uranium", "ores");
	public static final TagKey<Item> URANIUM_RAW_ORES = createResource("uranium", "raw_ores");
	public static final TagKey<Item> LEAD_BLOCKS = createResource("lead", "blocks");
	public static final TagKey<Item> TIN_BLOCKS = createResource("tin", "blocks");
	public static final TagKey<Item> URANIUM_BLOCKS = createResource("uranium", "blocks");
	public static final TagKey<Item> BRONZE_BLOCKS = createResource("bronze", "blocks");
	public static final TagKey<Item> STEEL_BLOCKS = createResource("steel", "blocks");
	public static final TagKey<Item> DIAMONDS = create("c:diamonds", "forge:gems/diamond");
	public static final TagKey<Item> WOODEN_CHESTS = create("c:wooden_chests", "forge:chests/wooden");
	public static final TagKey<Item> RUBBER_LOGS = create("rubber_logs");
	public static final TagKey<Item> FORGE_HAMMERS = create("forge_hammers");
	public static final TagKey<Item> WIRE_CUTTERS = create("wire_cutters");

	public static void init()
	{
	}

	private static TagKey<Item> create(String name)
	{
		return TagKey.create(Registries.ITEM, IC2.getIdentifier(name));
	}

	private static TagKey<Item> create(String fabricName, String forgeName)
	{
		ResourceLocation id = ResourceLocation.parse(IC2.envProxy.isFabricEnv() ? fabricName : forgeName);
		return TagKey.create(Registries.ITEM, id);
	}

	private static TagKey<Item> createResource(String material, String form)
	{
		return create("c:%s_%s".formatted(material, form), "forge:%s/%s".formatted(form, material));
	}
}
