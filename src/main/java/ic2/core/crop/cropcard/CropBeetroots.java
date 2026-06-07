package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropType;
import ic2.core.crop.CropVanilla;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropBeetroots extends CropVanilla
{
	public CropBeetroots(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.BEETROOTS_CROP;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(1, 0, 4, 0, 1, 2);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Red", "Food", "Beetroot" };
	}

	@Override
	public ItemStack getProduct()
	{
		return new ItemStack(Items.f_42732_, 1);
	}

	@Override
	public ItemStack getSeeds()
	{
		return new ItemStack(Items.f_42733_);
	}
}
