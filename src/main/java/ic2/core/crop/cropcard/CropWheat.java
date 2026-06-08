package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.CropVanilla;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropWheat extends CropVanilla
{
	public CropWheat(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.WHEAT_CROP;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(1, 0, 4, 0, 0, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Yellow", "Food", "Wheat" };
	}

	@Override
	public ItemStack getProduct()
	{
		return new ItemStack(Items.WHEAT, 1);
	}

	@Override
	public ItemStack getSeeds()
	{
		return new ItemStack(Items.WHEAT_SEEDS);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}
}
