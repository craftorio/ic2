package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.IC2;
import ic2.core.crop.CropBase;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropBaseSapling extends CropBase
{
	protected final Block cropBlock;
	protected final String saplingName;
	protected final ItemStack cropSapling;

	public CropBaseSapling(ICropType cropType, Block cropBlock, String saplingName, ItemStack cropDrop, ItemStack cropSapling)
	{
		super(cropType, cropDrop);
		this.cropBlock = cropBlock;
		this.saplingName = "ic2.crop." + saplingName;
		this.cropSapling = cropSapling;
	}

	@Override
	public Block getCropBlock()
	{
		return this.cropBlock;
	}

	@Override
	public String getSeedType()
	{
		return this.saplingName;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Speiger";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(3, 1, 0, 4, 4, 0);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Leaves", "Sapling", "Green" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	@Override
	public ItemStack[] getGains(ICropTile crop)
	{
		List<ItemStack> drops = new ArrayList<>();
		drops.add(this.cropDrop.m_41777_());
		if (IC2.random.nextInt(100) >= 75)
		{
			drops.add(this.cropSapling.m_41777_());
		}

		if (this.getId().equalsIgnoreCase("oak_sapling") && IC2.random.nextInt(100) >= 75)
		{
			drops.add(new ItemStack(Items.f_42410_));
		}

		return drops.toArray(new ItemStack[drops.size()]);
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getMaxAge() - 1 ? 150 : 600;
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}
}
