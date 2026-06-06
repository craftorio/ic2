package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class CropCoffee extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "coffee";
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Snoochy";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(7, 1, 4, 1, 2, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Leaves", "Ingredient", "Beans" };
	}

	@Override
	public int getMaxSize()
	{
		return 5;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() < 5 && crop.getLightLevel() >= 9;
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (0.4 * humidity + 1.4 * nutrients + 1.2 * air);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		if (crop.getCurrentSize() == 3)
		{
			return (int) (super.getGrowthDuration(crop) * 0.5);
		} else
		{
			return crop.getCurrentSize() == 4 ? (int) (super.getGrowthDuration(crop) * 1.5) : super.getGrowthDuration(crop);
		}
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentSize() >= 4;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return crop.getCurrentSize() == 4 ? null : ItemName.crop_res.getItemStack(CropResItemType.coffee_beans);
	}

	@Override
	public int getSizeAfterHarvest(ICropTile crop)
	{
		return 3;
	}
}
