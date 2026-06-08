package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropRedWheat extends Ic2CropCard
{
	public CropRedWheat(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.RED_WHEAT_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "raa1337";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(6, 3, 0, 0, 2, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Red", "Redstone", "Wheat" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5;
	}

	@Override
	public double dropGainChance()
	{
		return 0.5;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		BlockPos coords = crop.getPosition();
		return crop.getWorldObj().getBestNeighborSignal(coords) <= 0 && !crop.getWorldObj().random.nextBoolean()
			? new ItemStack(Items.WHEAT, 1)
			: new ItemStack(Items.REDSTONE, 1);
	}

	@Override
	public boolean isRedstoneSignalEmitter(ICropTile crop)
	{
		return true;
	}

	@Override
	public int getEmittedRedstoneSignal(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? 15 : 0;
	}

	@Override
	public int getEmittedLight(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() ? 7 : 0;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 600;
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 1;
	}
}
