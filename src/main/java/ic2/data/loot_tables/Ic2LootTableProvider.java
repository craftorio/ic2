package ic2.data.loot_tables;

import com.google.common.collect.Multimap;
import ic2.core.IC2;
import ic2.core.util.LogCategory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootDataId;
import net.minecraft.world.level.storage.loot.LootDataResolver;
import net.minecraft.world.level.storage.loot.LootDataType;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTable.Builder;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

public abstract class Ic2LootTableProvider extends LootTableProvider
{
	private final PackOutput packOutput;

	public Ic2LootTableProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider)
	{
		super(packOutput, Set.of(), List.of());
		this.packOutput = packOutput;
	}

	@Override
	public CompletableFuture<?> run(CachedOutput writer)
	{
		this.generate((id, builder) ->
		{
			LootTable table = builder.setParamSet(this.getContextType()).build();
			LootDataResolver resolver = new LootDataResolver()
			{
				@Override
				@javax.annotation.Nullable
				public <T> T getElement(LootDataId<T> id)
				{
					return null;
				}
			};
			ValidationContext lootTableReporter = new ValidationContext(LootContextParamSets.ALL_PARAMS, resolver);
			table.validate(lootTableReporter);
			Multimap<String, String> multimap = lootTableReporter.getProblems();
			if (!multimap.isEmpty())
			{
				multimap.forEach((name, message) -> IC2.log.warn(LogCategory.General, "Found validation problem in {}: {}", name, message));
				throw new IllegalStateException("Failed to validate loot tables, see logs");
			}

			Path path = this.packOutput.getOutputFolder()
				.resolve("data/" + id.getNamespace() + "/loot_tables/" + id.getPath() + ".json");

			try
			{
				DataProvider.saveStable(writer, LootDataType.TABLE.parser().toJsonTree(table), path);
			} catch (Exception var6)
			{
				IC2.log.error(LogCategory.General, "Couldn't save loot table {}", path, var6);
			}
		});
		return CompletableFuture.allOf();
	}

	public abstract void generate(BiConsumer<ResourceLocation, Builder> var1);

	protected abstract LootContextParamSet getContextType();
}
