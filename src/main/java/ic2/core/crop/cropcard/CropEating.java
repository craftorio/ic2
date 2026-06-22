package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;
import ic2.api.item.ItemWrapper;
import ic2.core.IC2;
import ic2.core.Ic2DamageSource;
import ic2.core.crop.Ic2CropCard;
import ic2.core.proxy.EnvProxy;
import ic2.core.ref.Ic2Blocks;
import ic2.core.util.BiomeUtil;
import ic2.core.util.StackUtil;

import java.util.Collections;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

public class CropEating extends Ic2CropCard
{
	private final double movementMultiplier = 0.5;
	private final double length = 1.0;

	public CropEating(ICropType cropType)
	{
		super(cropType);
	}

	private static boolean hasMetalAromor(LivingEntity entity)
	{
		if (!(entity instanceof Player player))
		{
			return false;
		} else
		{
			for (ItemStack stack : player.getInventory().armor)
			{
				if (stack != null && ItemWrapper.isMetalArmor(stack, player))
				{
					return true;
				}
			}

			return false;
		}
	}

	@Override
	public Block getCropBlock()
	{
		return Ic2Blocks.EATING_PLANT_CROP;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Hasudako";
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(6, 1, 1, 3, 1, 4);
	}

	@Override
	public String[] getAttributes()
	{
		return new String[] { "Bad", "Food" };
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < 2
			? crop.getLightLevel() > 10
			: crop.isBlockBelow(Blocks.LAVA) && crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() > 10;
	}

	@Override
	public int getOptimalHarvestAge(ICropTile crop)
	{
		return this.getMaxAge() - 2;
	}

	@Override
	public boolean canBeHarvested(ICropTile crop)
	{
		return crop.getCurrentAge() >= this.getOptimalHarvestAge(crop) && crop.getCurrentAge() < this.getMaxAge();
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return this.canBeHarvested(crop) ? new ItemStack(Blocks.CACTUS) : null;
	}

	@Override
	public void tick(ICropTile crop)
	{
		if (crop.getCurrentAge() != 0)
		{
			BlockPos coords = crop.getPosition();
			double xcentered = coords.getX() + 0.5;
			double ycentered = coords.getY() + 0.5;
			double zcentered = coords.getZ() + 0.5;
			if (crop.getCustomData().getBoolean("eaten"))
			{
				StackUtil.dropAsEntity(crop.getWorldObj(), coords, new ItemStack(Items.ROTTEN_FLESH));
				crop.getCustomData().putBoolean("eaten", false);
			}

			List<LivingEntity> list = crop.getWorldObj()
				.getEntitiesOfClass(
					LivingEntity.class,
					new AABB(xcentered - 1.0, coords.getY(), zcentered - 1.0, xcentered + 1.0, coords.getY() + 1.0 + 1.0, zcentered + 1.0),
					EntitySelector.NO_CREATIVE_OR_SPECTATOR
				);
			if (!list.isEmpty())
			{
				Collections.shuffle(list);

				for (LivingEntity entity : list)
				{
					if (!(entity instanceof Player) || !((Player) entity).getAbilities().instabuild)
					{
						entity.setDeltaMovement((xcentered - entity.getX()) * 0.5, Math.min(entity.getDeltaMovement().y(), -0.05), (zcentered - entity.getZ()) * 0.5);
						entity.hurt(Ic2DamageSource.create(crop.getWorldObj(), "cropEating"), (crop.getCurrentAge() + 1) * 2.0F);
						if (!hasMetalAromor(entity))
						{
							entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 64, 50));
							entity.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 64, 0));
							entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 64, 0));
						}

						if (this.canGrow(crop))
						{
							crop.setGrowthPoints(crop.getGrowthPoints() + 100);
						}

						crop.getWorldObj()
							.playSound(null, xcentered, ycentered, zcentered, SoundEvents.GENERIC_EAT, SoundSource.BLOCKS, 1.0F, IC2.random.nextFloat() * 0.1F + 0.9F);
						crop.getCustomData().putBoolean("eaten", true);
						break;
					}
				}
			}
		}
	}

	@Override
	public int getRootsLength(ICropTile crop)
	{
		return 5;
	}

	@Override
	public int getGrowthDuration(ICropTile crop)
	{
		float multiplier = 1.0F;
		BlockPos coords = crop.getPosition();
		Holder<Biome> biome = BiomeUtil.getBiome(crop.getWorldObj(), coords);
		if (IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.SWAMP) || IC2.envProxy.biomeHasType(biome, EnvProxy.BiomeType.MOUNTAIN))
		{
			multiplier /= 1.5F;
		}

		multiplier /= 1.0F + crop.getTerrainAirQuality() / 10.0F;
		return (int) (super.getGrowthDuration(crop) * multiplier);
	}
}
