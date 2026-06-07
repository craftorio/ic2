package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropCocoa extends Ic2CropCard
{
	public CropCocoa(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.COCOA_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(3, 1, 3, 0, 4, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Brown", "Food", "Stem" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() <= this.getMaxAge() - 1 && crop.getStorageNutrients() >= 3;
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (humidity * 0.8 + nutrients * 1.3 + air * 0.9);
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.f_42533_);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 900 : 400;
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}
}
