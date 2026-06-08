package ic2.data.loot_tables;

import com.google.common.collect.Multimap;
import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.function.BiConsumer;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataGenerator.PathProvider;
import net.minecraft.data.DataGenerator.Target;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class Ic2LootTableProvider extends LootTableProvider
{
	private final PathProvider pathResolver;

	public Ic2LootTableProvider(DataGenerator dataGenerator)
	{
		super(dataGenerator);
		this.pathResolver = dataGenerator.createPathProvider(Target.DATA_PACK, "loot_tables");
	}

	public void run(CachedOutput writer)
	{
		this.generate((id, builder) ->
		{
			LootTable table = builder.setParamSet(this.getContextType()).build();
			ValidationContext lootTableReporter = new ValidationContext(LootContextParamSets.ALL_PARAMS, identifier -> null, identifier -> table);
			LootTables.validate(lootTableReporter, id, table);
			Multimap<String, String> multimap = lootTableReporter.getProblems();
			if (!multimap.isEmpty())
			{
				multimap.forEach((name, message) -> IC2.log.warn(LogCategory.General, "Found validation problem in {}: {}", name, message));
				throw new IllegalStateException("Failed to validate loot tables, see logs");
			}

			Path path = this.pathResolver.json(id);

			try
			{
				DataProvider.saveStable(writer, LootTables.serialize(table), path);
			} catch (IOException var6)
			{
				IC2.log.error(LogCategory.General, "Couldn't save loot table {}", path, var6);
			}
		});
	}

	public abstract void generate(BiConsumer<ResourceLocation, Builder> var1);

	protected abstract LootContextParamSet getContextType();
}
