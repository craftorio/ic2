package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.CropBase;

import java.util.Collection;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropBaseMetalCommon extends CropBase
{
	protected final String[] cropAttributes;
	protected final Block cropBlock;
	protected final Collection<TagKey<Block>> cropRootsRequirement;

	public CropBaseMetalCommon(ICropType cropType, Block cropBlock, String[] cropAttributes, Collection<TagKey<Block>> cropRootsRequirement, ItemStack cropDrop)
	{
		super(cropType, cropDrop);
		this.cropBlock = cropBlock;
		this.cropAttributes = cropAttributes;
		this.cropRootsRequirement = cropRootsRequirement;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(6, 2, 0, 0, 1, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return this.cropAttributes;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		int sizeReq = this.getMaxAge() - 1;
		if (crop.getCurrentAge() < sizeReq)
		{
			return true;
		}

		if (crop.getCurrentAge() == sizeReq)
		{
			if (this.cropRootsRequirement == null || this.cropRootsRequirement.isEmpty())
			{
				return true;
			}

			for (TagKey<Block> tag : this.cropRootsRequirement)
			{
				if (crop.isBlockBelow(tag))
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return this.cropDrop.m_41777_();
	}

	@Override
	public double dropGainChance()
	{
		return super.dropGainChance() / 2.0;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 2000 : 800;
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 1;
	}
}
