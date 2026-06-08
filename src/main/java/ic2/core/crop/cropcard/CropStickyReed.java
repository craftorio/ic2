package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.IC2;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropStickyReed extends Ic2CropCard
{
	public CropStickyReed(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.STICKY_REED_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "raa1337";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(4, 2, 0, 1, 0, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Reed", "Resin" };
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (humidity * 1.2 + nutrients + air * 0.8);
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() > 0;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return crop.getCurrentAge() <= this.getMaxAge() - 1 ? new ItemStack(Items.SUGAR_CANE, crop.getCurrentAge()) : new ItemStack(Ic2Items.RESIN);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? (byte) (2 - IC2.random.nextInt(2)) : 0;
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return false;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? 400 : 100;
	}
}
