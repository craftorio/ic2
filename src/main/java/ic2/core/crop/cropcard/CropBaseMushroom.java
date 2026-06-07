package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.CropBase;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropBaseMushroom extends CropBase
{
	protected final String[] cropAttributes;
	protected final Block cropBlock;

	public CropBaseMushroom(ICropType cropType, Block cropBlock, String[] cropAttributes, ItemStack cropDrop)
	{
		super(cropType, cropDrop);
		this.cropAttributes = cropAttributes;
		this.cropBlock = cropBlock;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 4, 0, 0, 4);
	}

	@Override
	public String[] getAttributes()
	{
		return this.cropAttributes;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return this.cropDrop.m_41777_();
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 200;
	}
}
