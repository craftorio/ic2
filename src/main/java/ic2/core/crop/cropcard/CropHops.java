package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropHops extends Ic2CropCard
{
	public CropHops(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.HOPS_CROP;
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
	public int getGrowthDuration(ICropTile crop)
	{
		return 600;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Ic2Items.HOPS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}
}
