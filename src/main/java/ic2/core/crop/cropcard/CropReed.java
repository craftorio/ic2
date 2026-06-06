package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropReed extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "reed";
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 0, 1, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Reed" };
	}

	@Override
	public int getMaxSize()
	{
		return 3;
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (humidity * 1.2 + nutrients + air * 0.8);
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentSize() > 1;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.REEDS, crop.getCurrentSize() - 1);
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return false;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 200;
	}
}
