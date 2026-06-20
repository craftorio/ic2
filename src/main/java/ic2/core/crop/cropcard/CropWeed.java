package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class CropWeed extends Ic2CropCard
{
	public CropWeed(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.WEED_CROP;
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
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return 1;
	}

	@Override
	public boolean onLeftClick(ICropTile crop, Player player)
	{
		return false;
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
