package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropColorFlower extends Ic2CropCard
{
	public String name;
	public Block cropBlock;
	public String[] attributes;
	public DyeColor color;

	public CropColorFlower(ICropType cropType, Block cropBlock, String[] attributes, DyeColor color)
	{
		super(cropType);
		this.name = cropType.getName();
		this.cropBlock = cropBlock;
		this.attributes = attributes;
		this.color = color;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public String getDiscoveredBy()
	{
		return !this.name.equals("dandelion") && !this.name.equals("rose") ? "Alblaka" : "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 1, 1, 0, 5, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return this.attributes;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() <= this.getMaxAge() - 1 && crop.getLightLevel() >= 12;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(DyeItem.m_41082_(this.color));
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 600 : 400;
	}
}
