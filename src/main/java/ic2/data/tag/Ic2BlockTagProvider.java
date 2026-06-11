package ic2.data.tag;

import ic2.compat.AbstractBlockTagProvider;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Blocks;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider.IntrinsicTagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class Ic2BlockTagProvider extends AbstractBlockTagProvider
{
	private static final Set<Block> unconnectableBlockList = Set.of(
		Ic2Blocks.NUKE, Ic2Blocks.ITNT, Ic2Blocks.ITEM_BUFFER, Ic2Blocks.OBSCURED_WALL, Ic2Blocks.IRON_FURNACE
	);

	public Ic2BlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper)
	{
		super(output, lookupProvider, existingFileHelper);
	}

	protected IntrinsicTagAppender<Block> tag(TagKey<Block> tag)
	{
		return super.tag(tag);
	}

	protected void addTags(HolderLookup.Provider lookupProvider)
	{
		BuiltInRegistries.BLOCK.forEach(block ->
		{
			if (block instanceof AbstractCableBlock)
			{
				this.tag(Ic2BlockTags.CABLE_CONNECTABLE).add(block);
			} else if (block instanceof Ic2TileEntityBlock tileEntityBlock)
			{
				if (this.canCableConnect(block))
				{
					if (!tileEntityBlock.getTeClass().equals(TileEntityCrop.class))
					{
						this.tag(Ic2BlockTags.CABLE_CONNECTABLE).add(block);
					}
				}
			}
		});
		this.tag(Ic2BlockTags.RUBBER_LOGS)
			.add(Ic2Blocks.RUBBER_LOG)
			.add(Ic2Blocks.RUBBER_WOOD)
			.add(Ic2Blocks.STRIPPED_RUBBER_LOG)
			.add(Ic2Blocks.STRIPPED_RUBBER_WOOD);
		this.tag(BlockTags.LOGS).addTag(Ic2BlockTags.RUBBER_LOGS);
		this.tag(BlockTags.PLANKS).add(Ic2Blocks.RUBBER_PLANKS);
		this.tag(BlockTags.LOGS_THAT_BURN).addTag(Ic2BlockTags.RUBBER_LOGS);
	}

	public boolean canCableConnect(Block block)
	{
		String identifierPath = BuiltInRegistries.BLOCK.getKey(block).getPath();
		return !unconnectableBlockList.contains(block) && !identifierPath.contains("storage_box") && !identifierPath.contains("tank");
	}
}
