package ic2.api.crops;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.List;

public abstract class CropCard
{
	public abstract String getId();

	public abstract String getOwner();

	public String getUnlocalizedName()
	{
		return getOwner() + ".crop." + getId();
	}

	public String getDiscoveredBy()
	{
		return "unknown";
	}

	public String desc(int i)
	{
		String[] att = getAttributes();
		if (att == null || att.length == 0)
			return "";
		if (i == 0)
		{
			String str = att[0];
			if (att.length >= 2)
			{
				str = str + ", " + att[1];
				if (att.length >= 3)
					str = str + ",";
			}
			return str;
		}
		if (att.length < 3)
			return "";
		String s = att[2];
		if (att.length >= 4)
			s = s + ", " + att[3];
		return s;
	}

	public int getRootsLength(ICropTile cropTile)
	{
		return 1;
	}

	public abstract CropProperties getProperties();

	public String[] getAttributes()
	{
		return new String[0];
	}

	public String getSeedType()
	{
		return "ic2.crop.seeds";
	}

	public abstract int getMaxSize();

	public int getGrowthDuration(ICropTile cropTile)
	{
		return getProperties().getTier() * 200;
	}

	public boolean canGrow(ICropTile cropTile)
	{
		return (cropTile.getCurrentSize() < getMaxSize());
	}

	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return humidity + nutrients + air;
	}

	public boolean canCross(ICropTile crop)
	{
		return (crop.getCurrentSize() >= 3);
	}

	public boolean onRightClick(ICropTile cropTile, EntityPlayer player)
	{
		return cropTile.performManualHarvest();
	}

	public int getOptimalHarvestSize(ICropTile cropTile)
	{
		return getMaxSize();
	}

	public boolean canBeHarvested(ICropTile cropTile)
	{
		return (cropTile.getCurrentSize() == getMaxSize());
	}

	public double dropGainChance()
	{
		return Math.pow(0.95D, getProperties().getTier());
	}

	@Deprecated
	public ItemStack getGain(ICropTile crop)
	{
		return ItemStack.EMPTY;
	}

	public ItemStack[] getGains(ICropTile crop)
	{
		return new ItemStack[] { getGain(crop) };
	}

	public int getSizeAfterHarvest(ICropTile cropTile)
	{
		return 1;
	}

	public boolean onLeftClick(ICropTile cropTile, EntityPlayer player)
	{
		return cropTile.pick();
	}

	public float dropSeedChance(ICropTile crop)
	{
		if (crop.getCurrentSize() == 1)
			return 0.0F;
		float base = 0.5F;
		if (crop.getCurrentSize() == 2)
			base /= 2.0F;
		for (int i = 0; i < getProperties().getTier(); i++)
			base = (float) (base * 0.8D);
		return base;
	}
  
	public ItemStack getSeeds(ICropTile crop)
	{
		return crop.generateSeeds(crop.getCrop(), crop.getStatGrowth(), crop.getStatGain(), crop.getStatResistance(), crop.getScanLevel());
	}

	public boolean isRedstoneSignalEmitter(ICropTile cropTile)
	{
		return false;
	}

	public int getEmittedRedstoneSignal(ICropTile cropTile)
	{
		return 0;
	}

	public int getEmittedLight(ICropTile crop)
	{
		return 0;
	}

	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return (entity instanceof net.minecraft.entity.EntityLivingBase && entity.isSprinting());
	}

	public void tick(ICropTile cropTile)
	{
	}

	public boolean isWeed(ICropTile cropTile)
	{
		return (cropTile.getCurrentSize() >= 2 && (cropTile.getCrop() == Crops.weed || cropTile.getStatGrowth() >= 24));
	}

	public World getWorld(ICropTile cropTile)
	{
		return cropTile.getWorldObj();
	}

	@SideOnly(Side.CLIENT)
	public abstract List<ResourceLocation> getTexturesLocation();
}
