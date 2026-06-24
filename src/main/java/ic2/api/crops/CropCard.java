package ic2.api.crops;

import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class CropCard
{
	protected final ICropType cropType;

	public CropCard(ICropType cropType)
	{
		this.cropType = cropType;
	}

	public String getId()
	{
		return this.cropType.getName();
	}

	public String getOwner()
	{
		return this.cropType.getOwner();
	}

	public abstract Block getCropBlock();

	public String getUnlocalizedName()
	{
		return this.getOwner() + ".crop." + this.getId();
	}

	public String getDiscoveredBy()
	{
		return "unknown";
	}

	public String desc(int i)
	{
		String[] att = this.getAttributes();
		if (att == null || att.length == 0)
		{
			return "";
		}

		if (i == 0)
		{
			String s = att[0];
			if (att.length >= 2)
			{
				s = s + ", " + att[1];
				if (att.length >= 3)
				{
					s = s + ",";
				}
			}

			return s;
		} else
		{
			if (att.length < 3)
			{
				return "";
			}

			String s = att[2];
			if (att.length >= 4)
			{
				s = s + ", " + att[3];
			}

			return s;
		}
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

	public int getMaxAge()
	{
		return this.cropType.getMaxAge();
	}

	public int getGrowthDuration(ICropTile cropTile)
	{
		return this.getProperties().tier() * 200;
	}

	public boolean canGrow(ICropTile cropTile)
	{
		return cropTile.getCurrentAge() < this.getMaxAge();
	}

	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return humidity + nutrients + air;
	}

	public boolean canCross(ICropTile crop)
	{
		return crop.getCurrentAge() >= 2;
	}

	public boolean onRightClick(ICropTile cropTile, Player player)
	{
		return cropTile.performManualHarvest();
	}

	public int getOptimalHarvestAge(ICropTile cropTile)
	{
		return this.getMaxAge();
	}

	public boolean canBeHarvested(ICropTile cropTile)
	{
		return cropTile.getCurrentAge() == this.getMaxAge();
	}

	public double dropGainChance()
	{
		return Math.pow(0.95, this.getProperties().tier());
	}

	@Deprecated
	public ItemStack getGain(ICropTile crop)
	{
		return ItemStack.EMPTY;
	}

	public ItemStack[] getGains(ICropTile crop)
	{
		return new ItemStack[] { this.getGain(crop) };
	}

	public int getAgeAfterHarvest(ICropTile cropTile)
	{
		return 0;
	}

	public boolean onLeftClick(ICropTile cropTile, Player player)
	{
		return cropTile.pick();
	}

	public float dropSeedChance(ICropTile crop)
	{
		if (crop.getCurrentAge() == 0)
		{
			return 0.0F;
		}

		float base = 0.5F;
		if (crop.getCurrentAge() == 1)
		{
			base /= 2.0F;
		}

		for (int i = 0; i < this.getProperties().tier(); i++)
		{
			base = (float) (base * 0.8);
		}

		return base;
	}

	public ItemStack getSeeds(ICropTile crop)
	{
		return crop.generateSeeds(crop.getCrop(), crop.getStatGrowth(), crop.getStatGain(), crop.getStatResistance(), crop.getScanLevel());
	}

	public void onNeighbourChange(ICropTile crop)
	{
	}

	public boolean isRedstoneSignalEmitter(ICropTile cropTile)
	{
		return false;
	}

	public int getEmittedRedstoneSignal(ICropTile cropTile)
	{
		return 0;
	}

	public void onBlockDestroyed(ICropTile crop)
	{
	}

	public int getEmittedLight(ICropTile crop)
	{
		return 0;
	}

	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		return entity instanceof LivingEntity && entity.isSprinting();
	}

	public void tick(ICropTile cropTile)
	{
	}

	public boolean isWeed(ICropTile cropTile)
	{
		return cropTile.getCurrentAge() >= 1 && (cropTile.getCrop() == Crops.weed || cropTile.getStatGrowth() >= 24);
	}

	public Level getWorld(ICropTile cropTile)
	{
		return cropTile.getWorldObj();
	}

	@OnlyIn(Dist.CLIENT)
	public abstract List<ResourceLocation> getTexturesLocation();
}
