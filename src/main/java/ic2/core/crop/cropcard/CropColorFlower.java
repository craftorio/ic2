package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropColorFlower extends IC2CropCard
{
	public final String name;
	public final String[] attributes;
	public final int color;

	public CropColorFlower(String n, String[] a, int c)
	{
		this.name = n;
		this.attributes = a;
		this.color = c;
	}

	@Override
	public String getDiscoveredBy()
	{
		return !this.name.equals("dandelion") && !this.name.equals("rose") ? "Alblaka" : "Notch";
	}

	@Override
	public String getId()
	{
		return this.name;
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
	public int getMaxSize()
	{
		return 4;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() <= 3 && crop.getLightLevel() >= 12;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentSize() == 4;
	}

	@Override
	public int getOptimalHarvestSize(ICropTile crop)
	{
		return 4;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.DYE, 1, this.color);
	}

	@Override
	public int getSizeAfterHarvest(ICropTile crop)
	{
		return 3;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentSize() == 3 ? 600 : 400;
	}
}
