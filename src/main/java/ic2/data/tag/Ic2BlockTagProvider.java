package ic2.data.tag;

import ic2.compat.AbstractBlockTagProvider;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.block.wiring.AbstractCableBlock;
import ic2.core.crop.TileEntityCrop;
import ic2.core.ref.Ic2BlockTags;
import ic2.core.ref.Ic2Blocks;

import java.util.Set;

import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.TagsProvider.TagAppender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class Ic2BlockTagProvider extends AbstractBlockTagProvider
{
	private static final Set<Block> unconnectableBlockList = Set.of(
		Ic2Blocks.CLASSIC_NUKE, Ic2Blocks.NUKE, Ic2Blocks.ITNT, Ic2Blocks.ITEM_BUFFER, Ic2Blocks.OBSCURED_WALL, Ic2Blocks.IRON_FURNACE
	);

	public Ic2BlockTagProvider(DataGenerator root)
	{
		super(root);
	}

	protected TagAppender<Block> tag(TagKey<Block> tag)
	{
		return this.m_206424_(tag);
	}

	protected void m_6577_()
	{
		Registry.BLOCK.forEach(block ->
		{
			if (block instanceof AbstractCableBlock)
			{
				this.tag(Ic2BlockTags.CABLE_CONNECTABLE).m_126582_(block);
			} else if (block instanceof Ic2TileEntityBlock tileEntityBlock)
			{
				if (this.canCableConnect(block))
				{
					if (!tileEntityBlock.getTeClass().equals(TileEntityCrop.class))
					{
						this.tag(Ic2BlockTags.CABLE_CONNECTABLE).m_126582_(block);
					}
				}
			}
		});
		this.tag(Ic2BlockTags.RUBBER_LOGS)
			.m_126582_(Ic2Blocks.RUBBER_LOG)
			.m_126582_(Ic2Blocks.RUBBER_WOOD)
			.m_126582_(Ic2Blocks.STRIPPED_RUBBER_LOG)
			.m_126582_(Ic2Blocks.STRIPPED_RUBBER_WOOD);
		this.tag(BlockTags.f_13106_).m_206428_(Ic2BlockTags.RUBBER_LOGS);
		this.tag(BlockTags.f_13090_).m_126582_(Ic2Blocks.RUBBER_PLANKS);
		this.tag(BlockTags.f_13105_).m_206428_(Ic2BlockTags.RUBBER_LOGS);
	}

	public boolean canCableConnect(Block block)
	{
		String identifierPath = Registry.BLOCK.getKey(block).m_135815_();
		return !unconnectableBlockList.contains(block) && !identifierPath.contains("storage_box") && !identifierPath.contains("tank");
	}
}
