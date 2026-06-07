package ic2.data.loot_tables.generator;

import ic2.core.ref.Ic2Blocks;

public class CableLootTableGenerator extends Ic2BlockLootTableGenerator
{
	@Override
	public Ic2BlockLootTableGenerator build()
	{
		this.addDrop(Ic2Blocks.COPPER_CABLE);
		this.addDrop(Ic2Blocks.INSULATED_COPPER_CABLE);
		this.addDrop(Ic2Blocks.TIN_CABLE);
		this.addDrop(Ic2Blocks.INSULATED_TIN_CABLE);
		this.addDrop(Ic2Blocks.GOLD_CABLE);
		this.addDrop(Ic2Blocks.INSULATED_GOLD_CABLE);
		this.addDrop(Ic2Blocks.DOUBLE_INSULATED_GOLD_CABLE);
		this.addDrop(Ic2Blocks.IRON_CABLE);
		this.addDrop(Ic2Blocks.INSULATED_IRON_CABLE);
		this.addDrop(Ic2Blocks.DOUBLE_INSULATED_IRON_CABLE);
		this.addDrop(Ic2Blocks.TRIPLE_INSULATED_IRON_CABLE);
		this.addDrop(Ic2Blocks.GLASS_FIBRE_CABLE);
		this.addDrop(Ic2Blocks.DETECTOR_CABLE);
		this.addDrop(Ic2Blocks.SPLITTER_CABLE);
		this.addDrop(Ic2Blocks.WHITE_WALL);
		this.addDrop(Ic2Blocks.ORANGE_WALL);
		this.addDrop(Ic2Blocks.MAGENTA_WALL);
		this.addDrop(Ic2Blocks.LIGHT_BLUE_WALL);
		this.addDrop(Ic2Blocks.YELLOW_WALL);
		this.addDrop(Ic2Blocks.LIME_WALL);
		this.addDrop(Ic2Blocks.PINK_WALL);
		this.addDrop(Ic2Blocks.GRAY_WALL);
		this.addDrop(Ic2Blocks.LIGHT_GRAY_WALL);
		this.addDrop(Ic2Blocks.CYAN_WALL);
		this.addDrop(Ic2Blocks.PURPLE_WALL);
		this.addDrop(Ic2Blocks.BLUE_WALL);
		this.addDrop(Ic2Blocks.BROWN_WALL);
		this.addDrop(Ic2Blocks.GREEN_WALL);
		this.addDrop(Ic2Blocks.RED_WALL);
		this.addDrop(Ic2Blocks.BLACK_WALL);
		return this;
	}
}
