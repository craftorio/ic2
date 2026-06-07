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
		this.pathResolver = dataGenerator.m_236036_(Target.DATA_PACK, "loot_tables");
	}

	public void m_213708_(CachedOutput writer)
	{
		this.generate((id, builder) ->
		{
			LootTable table = builder.m_79165_(this.getContextType()).m_79167_();
			ValidationContext lootTableReporter = new ValidationContext(LootContextParamSets.f_81420_, identifier -> null, identifier -> table);
			LootTables.m_79202_(lootTableReporter, id, table);
			Multimap<String, String> multimap = lootTableReporter.m_79352_();
			if (!multimap.isEmpty())
			{
				multimap.forEach((name, message) -> IC2.log.warn(LogCategory.General, "Found validation problem in {}: {}", name, message));
				throw new IllegalStateException("Failed to validate loot tables, see logs");
			}

			Path path = this.pathResolver.m_236048_(id);

			try
			{
				DataProvider.m_236072_(writer, LootTables.m_79200_(table), path);
			} catch (IOException var6)
			{
				IC2.log.error(LogCategory.General, "Couldn't save loot table {}", path, var6);
			}
		});
	}

	public abstract void generate(BiConsumer<ResourceLocation, Builder> var1);

	protected abstract LootContextParamSet getContextType();
}
