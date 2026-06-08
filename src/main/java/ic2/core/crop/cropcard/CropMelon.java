package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.IC2;
import ic2.core.crop.CropVanillaStem;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public class CropMelon extends CropVanillaStem
{
	public CropMelon(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.MELON_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Chao";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 0, 4, 0, 2, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Green", "Food", "Stem" };
	}

	@Override
	protected ItemStack getProduct()
	{
		return IC2.random.nextInt(3) == 0 ? new ItemStack(Blocks.MELON) : new ItemStack(Items.MELON, IC2.random.nextInt(4) + 2);
	}

	@Override
	protected ItemStack getSeeds()
	{
		return new ItemStack(Items.MELON_SEEDS, IC2.random.nextInt(2) + 1);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() == this.getMaxAge() - 1 ? 700 : 250;
	}
}
