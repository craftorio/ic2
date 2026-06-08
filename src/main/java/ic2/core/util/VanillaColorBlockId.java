package ic2.core.util;

import net.minecraft.core.Registry;
import net.minecraft.world.level.block.Block;

public enum VanillaColorBlockId
{
	WOOL("wool"),
	STAINED_GLASS("stained_glass"),
	STAINED_GLASS_PANE("stained_glass_pane"),
	BED("bed"),
	CANDLE("candle"),
	BANNER("banner"),
	WALL_BANNER("wall_banner"),
	TERRACOTTA("terracotta"),
	GLAZED_TERRACOTTA("glazed_terracotta"),
	CONCRETE("concrete"),
	CONCRETE_POWDER("concrete_powder"),
	CARPET("carpet"),
	SHULKER_BOX("shulker_box");

	public final String id;

	VanillaColorBlockId(String id)
	{
		this.id = id;
	}

	public boolean test(Block block)
	{
		return Registry.BLOCK.getKey(block).getPath().contains(this.id);
	}
}
