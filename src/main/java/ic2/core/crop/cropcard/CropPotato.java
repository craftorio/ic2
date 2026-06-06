package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropPotato extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "potato";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 4, 0, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Yellow", "Food", "Potato" };
	}

	@Override
	public int getMaxSize()
	{
		return 4;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() < 4 && crop.getLightLevel() >= 9;
	}

	@Override
	public int getOptimalHarvestSize(ICropTile crop)
	{
		return 3;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentSize() >= 3;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		if (crop.getCurrentSize() >= 4 && IC2.random.nextInt(20) <= 0)
		{
			return new ItemStack(Items.POISONOUS_POTATO);
		} else
		{
			return crop.getCurrentSize() >= 3 ? new ItemStack(Items.POTATO) : null;
		}
	}

	@Override
	public int getSizeAfterHarvest(ICropTile crop)
	{
		return 1;
	}
}
