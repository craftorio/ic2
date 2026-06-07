package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.crop.Ic2Crops;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropTerraWart extends Ic2CropCard
{
	public CropTerraWart(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.TERRA_WART_CROP;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(5, 2, 4, 0, 3, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Blue", "Aether", "Consumable", "Snow" };
	}

	@Override
	public double dropGainChance()
	{
		return 0.8;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Ic2Items.TERRA_WART);
	}

	@Override
	public void tick(ICropTile crop)
	{
		if (crop.isBlockBelow(Blocks.f_50125_))
		{
			if (this.canGrow(crop))
			{
				crop.setGrowthPoints(crop.getGrowthPoints() + 100);
			}
		} else if (crop.isBlockBelow(Blocks.f_50135_) && crop.getWorldObj().random.nextInt(300) == 0)
		{
			crop.setCrop(Ic2Crops.cropNetherWart);
		}
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}
}
