package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropFlax extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "flax";
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Eloraam";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(2, 1, 1, 2, 0, 1);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Silk", "Vine", "Addictive" };
	}

	@Override
	public int getMaxSize()
	{
		return 4;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() < 4 && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.STRING);
	}

	@Override
	public int getSizeAfterHarvest(ICropTile crop)
	{
		return 1;
	}
}
