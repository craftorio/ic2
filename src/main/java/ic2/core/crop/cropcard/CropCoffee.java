package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropCoffee extends Ic2CropCard
{
	public CropCoffee(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.COFFEE_CROP;
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
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (0.4 * humidity + 1.4 * nutrients + 1.2 * air);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		if (crop.getCurrentAge() == this.getMaxAge() - 2)
		{
			return (int) (super.getGrowthDuration(crop) * 0.5);
		} else
		{
			return crop.getCurrentAge() == this.getMaxAge() - 3 ? (int) (super.getGrowthDuration(crop) * 1.5) : super.getGrowthDuration(crop);
		}
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? null : new ItemStack(Ic2Items.COFFEE_BEANS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 2;
	}
}
