package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public final class Ic2BlockTags
{
	public static final TagKey<Block> EMPTY = create("ic2:empty", "ic2:empty");
	public static final TagKey<Block> ORES = create("c:ores", "forge:ores");
	public static final TagKey<Block> RUBBER_LOGS = create("c:rubber_logs", "forge:rubber_logs");
	public static final TagKey<Block> LEAD_ORES = create("c:lead_ores", "forge:ores/lead");
	public static final TagKey<Block> SILVER_ORES = create("c:silver_ores", "forge:ores/silver");
	public static final TagKey<Block> TIN_ORES = create("c:tin_ores", "forge:ores/tin");
	public static final TagKey<Block> COPPER_BLOCKS = create("c:copper_blocks", "forge:storage_blocks/copper");
	public static final TagKey<Block> GOLD_BLOCKS = create("c:gold_blocks", "forge:storage_blocks/gold");
	public static final TagKey<Block> IRON_BLOCKS = create("c:iron_blocks", "forge:storage_blocks/iron");
	public static final TagKey<Block> LEAD_BLOCKS = create("c:lead_blocks", "forge:storage_blocks/lead");
	public static final TagKey<Block> SILVER_BLOCKS = create("c:silver_blocks", "forge:storage_blocks/silver");
	public static final TagKey<Block> TIN_BLOCKS = create("c:tin_blocks", "forge:storage_blocks/tin");
	public static final TagKey<Block> CABLE_CONNECTABLE = create("c:cable_connectable", "forge:cable_connectable");

	public static void init()
	{
	}

	private static TagKey<Block> create(String fabricName, String forgeName)
	{
		ResourceLocation id = ResourceLocation.parse(IC2.envProxy.isFabricEnv() ? fabricName : forgeName);
		return TagKey.create(Registries.BLOCK, id);
	}
}
