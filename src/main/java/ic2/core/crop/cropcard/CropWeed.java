package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.crop.IC2CropCard;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class CropWeed extends IC2CropCard
{
	@Override
	public String getId()
	{
		return "weed";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(0, 0, 0, 1, 0, 5);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Weed", "Bad" };
	}

	@Override
	public int getMaxSize()
	{
		return 5;
	}

	@Override
	public int getOptimalHarvestSize(ICropTile crop)
	{
		return 1;
	}

	@Override
	public void onLeftClick(ICropTile crop, EntityPlayer player)
	{
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return false;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return null;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return 300;
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return false;
	}
}
