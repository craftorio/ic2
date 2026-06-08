package ic2.data.loot_tables;

import ic2.data.loot_tables.generator.CableLootTableGenerator;
import ic2.data.loot_tables.generator.EnergyBlockLootTableGenerator;
import ic2.data.loot_tables.generator.Ic2BlockLootTableGenerator;

import java.util.List;
import java.util.function.BiConsumer;

import net.minecraft.data.DataGenerator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public class BlockLootTableProvider extends Ic2LootTableProvider
{
	public BlockLootTableProvider(DataGenerator dataGenerator)
	{
		super(dataGenerator);
	}

	protected List<Ic2BlockLootTableGenerator> generatorList()
	{
		return List.of(new CableLootTableGenerator(), new EnergyBlockLootTableGenerator());
	}

	@Override
	public void generate(BiConsumer<ResourceLocation, Builder> consumer)
	{
		this.generatorList().forEach(generator -> generator.build().accept(consumer));
	}

	@Override
	protected LootContextParamSet getContextType()
	{
		return LootContextParamSets.BLOCK;
	}
}
