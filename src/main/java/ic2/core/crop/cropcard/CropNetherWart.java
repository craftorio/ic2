package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.crop.Ic2Crops;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropNetherWart extends Ic2CropCard
{
	public CropNetherWart(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.NETHER_WART_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 4, 2, 0, 2, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Red", "Nether", "Ingredient", "Soulsand" };
	}

	@Override
	public double dropGainChance()
	{
		return 2.0;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.NETHER_WART, 1);
	}

	@Override
	public void tick(ICropTile crop)
	{
		if (crop.isBlockBelow(Blocks.SOUL_SAND))
		{
			if (this.canGrow(crop))
			{
				crop.setGrowthPoints(crop.getGrowthPoints() + 100);
			}
		} else if (crop.isBlockBelow(Blocks.SNOW) && crop.getWorldObj().random.nextInt(300) == 0)
		{
			crop.setCrop(Ic2Crops.cropTerraWart);
		}
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}
}
