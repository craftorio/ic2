package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropType;
import ic2.core.crop.CropVanilla;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropCarrots extends CropVanilla
{
	public CropCarrots(ICropType cropType)
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
		return new String[] { "Orange", "Food", "Carrots" };
	}

	@Override
	public ItemStack getProduct()
	{
		return new ItemStack(Items.CARROT);
	}

	@Override
	public ItemStack getSeeds()
	{
		return new ItemStack(Items.CARROT);
	}
}
