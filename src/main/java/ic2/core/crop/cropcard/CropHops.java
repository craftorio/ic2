package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class CropHops extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "hops";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 2, 2, 0, 1, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Green", "Ingredient", "Wheat" };
	}

	@Override
	public int getMaxSize()
	{
		return 7;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 600;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() < 7 && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return ItemName.crop_res.getItemStack(CropResItemType.hops);
	}

	@Override
	public int getSizeAfterHarvest(ICropTile crop)
	{
		return 3;
	}
}
