package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.core.IC2;
import ic2.core.crop.Ic2CropCard;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class CropVenomilia extends Ic2CropCard
{
	public CropVenomilia(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.VENOMILIA_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "raGan";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(3, 3, 1, 3, 3, 3);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Purple", "Flower", "Tulip", "Poison" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() <= 3 && crop.getLightLevel() >= 12 || crop.getCurrentAge() == 4;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= 3;
	}

	@Override
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return 3;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		if (crop.getCurrentAge() == 4)
		{
			return new ItemStack(Ic2Items.GRIN_POWDER);
		} else
		{
			return crop.getCurrentAge() >= 3 ? new ItemStack(Items.PURPLE_DYE) : null;
		}
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return 2;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		return crop.getCurrentAge() >= 2 ? 600 : 400;
	}

	@Override
	public boolean onRightClick(ICropTile crop, Player player)
	{
		if (!player.isShiftKeyDown())
		{
			this.onEntityCollision(crop, player);
		}

		return crop.performManualHarvest();
	}

	@Override
	public boolean onLeftClick(ICropTile crop, Player player)
	{
		if (!player.isShiftKeyDown())
		{
			this.onEntityCollision(crop, player);
		}

		return crop.pick();
	}

	@Override
	public boolean onEntityCollision(ICropTile crop, Entity entity)
	{
		if (crop.getCurrentAge() == 4 && entity instanceof LivingEntity)
		{
			if (entity instanceof Player && entity.isShiftKeyDown() && IC2.random.nextInt(50) != 0)
			{
				return super.onEntityCollision(crop, entity);
			}

			((LivingEntity) entity).addEffect(new MobEffectInstance(MobEffects.POISON, (IC2.random.nextInt(10) + 5) * 20, 0));
			crop.setCurrentAge(3);
			crop.updateState();
		}

		return super.onEntityCollision(crop, entity);
	}

	@Override
	public boolean isWeed(ICropTile crop)
	{
		return crop.getCurrentAge() == 4 && crop.getStatGrowth() >= 8;
	}
}
