package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.IC2;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropPotato extends Ic2CropCard
{
	public CropPotato(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.CARROTS_CROP;
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
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < 4 && crop.getLightLevel() >= 9;
	}

	@Override
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		if (crop.getCurrentAge() >= this.getMaxAge() && IC2.random.nextInt(20) <= 0)
		{
			return new ItemStack(Items.f_42675_);
		} else
		{
			return crop.getCurrentAge() >= this.getMaxAge() - 1 ? new ItemStack(Items.f_42620_) : null;
		}
	}
}
