package ic2.data.tag;

import ic2.compat.AbstractItemTagProvider;
import ic2.core.IC2;
import ic2.core.ref.Ic2ItemTags;
import ic2.core.ref.Ic2Items;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class Ic2ItemTagProvider extends AbstractItemTagProvider
{
	public Ic2ItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, blockTags, existingFileHelper);
	}

	protected @NotNull IntrinsicTagAppender<Item> tag(@NotNull TagKey<Item> tag)
	{
		return super.tag(tag);
	}

	protected void addTags(HolderLookup.@NotNull Provider lookupProvider)
	{
		this.tag(Ic2ItemTags.COAL_DUSTS).add(Ic2Items.COAL_DUST);
		this.tag(Ic2ItemTags.COPPER_DUSTS).add(Ic2Items.COPPER_DUST);
		this.tag(Ic2ItemTags.DIAMOND_DUSTS).add(Ic2Items.DIAMOND_DUST);
		this.tag(Ic2ItemTags.GOLD_DUSTS).add(Ic2Items.GOLD_DUST);
		this.tag(Ic2ItemTags.IRON_DUSTS).add(Ic2Items.IRON_DUST);
		this.tag(Ic2ItemTags.LAPIS_DUSTS).add(Ic2Items.LAPIS_DUST);
		this.tag(Ic2ItemTags.LEAD_DUSTS).add(Ic2Items.LEAD_DUST);
		this.tag(Ic2ItemTags.OBSIDIAN_DUSTS).add(Ic2Items.OBSIDIAN_DUST);
		this.tag(Ic2ItemTags.SILVER_DUSTS).add(Ic2Items.SILVER_DUST);
		this.tag(Ic2ItemTags.STONE_DUSTS).add(Ic2Items.STONE_DUST);
		this.tag(Ic2ItemTags.SULFUR_DUSTS).add(Ic2Items.SULFUR_DUST);
		this.tag(Ic2ItemTags.TIN_DUSTS).add(Ic2Items.TIN_DUST);
		this.tag(Ic2ItemTags.IRIDIUM_NUGGETS).add(Ic2Items.IRIDIUM_SHARD);
		this.tag(Ic2ItemTags.IRON_PLATES).add(Ic2Items.IRON_PLATE);
		this.tag(Ic2ItemTags.GOLD_PLATES).add(Ic2Items.GOLD_PLATE);
		this.tag(Ic2ItemTags.LEAD_PLATES).add(Ic2Items.LEAD_PLATE);
		this.tag(Ic2ItemTags.BRONZE_PLATES).add(Ic2Items.BRONZE_PLATE);
		this.tag(Ic2ItemTags.TIN_PLATES).add(Ic2Items.TIN_PLATE);
		this.tag(Ic2ItemTags.COPPER_PLATES).add(Ic2Items.COPPER_PLATE);
		this.tag(Ic2ItemTags.LAPIS_PLATES).add(Ic2Items.LAPIS_PLATE);
		this.tag(Ic2ItemTags.OBSIDIAN_PLATES).add(Ic2Items.OBSIDIAN_PLATE);
		this.tag(Ic2ItemTags.STEEL_PLATES).add(Ic2Items.STEEL_PLATE);
		this.tag(Ic2ItemTags.BRONZE_INGOTS).add(Ic2Items.BRONZE_INGOT);
		this.tag(Ic2ItemTags.LEAD_INGOTS).add(Ic2Items.LEAD_INGOT);
		this.tag(Ic2ItemTags.PLUTONIUM_INGOTS);
		this.tag(Ic2ItemTags.SILVER_INGOTS).add(Ic2Items.SILVER_INGOT);
		this.tag(Ic2ItemTags.STEEL_INGOTS).add(Ic2Items.STEEL_INGOT);
		this.tag(Ic2ItemTags.TIN_INGOTS).add(Ic2Items.TIN_INGOT);
		this.tag(Ic2ItemTags.URANIUM_INGOTS).add(Ic2Items.URANIUM_INGOT);
		this.tag(Ic2ItemTags.LEAD_ORES).add(Ic2Items.LEAD_ORE);
		this.tag(Ic2ItemTags.LEAD_ORES).add(Ic2Items.DEEPSLATE_LEAD_ORE);
		this.tag(Ic2ItemTags.LEAD_RAW_ORES).add(Ic2Items.RAW_LEAD);
		this.tag(Ic2ItemTags.SILVER_ORES);
		this.tag(Ic2ItemTags.TIN_ORES).add(Ic2Items.TIN_ORE);
		this.tag(Ic2ItemTags.TIN_ORES).add(Ic2Items.DEEPSLATE_TIN_ORE);
		this.tag(Ic2ItemTags.TIN_RAW_ORES).add(Ic2Items.RAW_TIN);
		this.tag(Ic2ItemTags.URANIUM_ORES).add(Ic2Items.URANIUM_ORE);
		this.tag(Ic2ItemTags.URANIUM_ORES).add(Ic2Items.DEEPSLATE_URANIUM_ORE);
		this.tag(Ic2ItemTags.URANIUM_RAW_ORES).add(Ic2Items.RAW_URANIUM);
		this.tag(Ic2ItemTags.ORES).addTag(Ic2ItemTags.LEAD_ORES).addTag(Ic2ItemTags.SILVER_ORES).addTag(Ic2ItemTags.TIN_ORES).addTag(Ic2ItemTags.URANIUM_ORES);
		this.tag(Ic2ItemTags.LEAD_BLOCKS).add(Ic2Items.LEAD_BLOCK);
		this.tag(Ic2ItemTags.URANIUM_BLOCKS).add(Ic2Items.URANIUM_BLOCK);
		this.tag(Ic2ItemTags.TIN_BLOCKS).add(Ic2Items.TIN_BLOCK);
		this.tag(Ic2ItemTags.BRONZE_BLOCKS).add(Ic2Items.BRONZE_BLOCK);
		this.tag(Ic2ItemTags.STEEL_BLOCKS).add(Ic2Items.STEEL_BLOCK);
		this.tag(Ic2ItemTags.DIAMONDS).add(Ic2Items.INDUTRIAL_DIAMOND);
		this.tag(Ic2ItemTags.RUBBER_LOGS).add(Ic2Items.RUBBER_LOG).add(Ic2Items.RUBBER_WOOD).add(Ic2Items.STRIPPED_RUBBER_LOG).add(Ic2Items.STRIPPED_RUBBER_WOOD);
		this.tag(ItemTags.LOGS).addTag(Ic2ItemTags.RUBBER_LOGS);
		this.tag(ItemTags.PLANKS).add(Ic2Items.RUBBER_PLANKS);
		this.tag(ItemTags.LOGS_THAT_BURN).addTag(Ic2ItemTags.RUBBER_LOGS);
		if (IC2.envProxy.isFabricEnv())
		{
			this.tag(Ic2ItemTags.WOODEN_CHESTS).add(Items.CHEST);
		}
	}
}
