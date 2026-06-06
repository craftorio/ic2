package ic2.core.item.armor;

import ic2.api.item.IHazmatLike;
import ic2.api.util.FluidContainerOutputMode;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.LiquidUtil;
import ic2.core.util.StackUtil;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ItemArmorHazmat extends ItemArmorUtility implements IHazmatLike
{
	public ItemArmorHazmat(ItemName name, EntityEquipmentSlot type)
	{
		super(name, "hazmat", type);
		this.setMaxDamage(64);
		if (this.armorType == EntityEquipmentSlot.FEET)
		{
			MinecraftForge.EVENT_BUS.register(this);
		}
	}

	@Override
	public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot)
	{
		if (this.armorType == EntityEquipmentSlot.HEAD && hazmatAbsorbs(source) && hasCompleteHazmat(player))
		{
			if (source == DamageSource.IN_FIRE || source == DamageSource.LAVA || source == DamageSource.HOT_FLOOR)
			{
				player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 60, 1));
			}

			return new ISpecialArmor.ArmorProperties(10, 1.0, Integer.MAX_VALUE);
		} else
		{
			return this.armorType == EntityEquipmentSlot.FEET && source == DamageSource.FALL
				? new ISpecialArmor.ArmorProperties(10, damage < 8.0 ? 1.0 : 0.875, (armor.getMaxDamage() - armor.getItemDamage() + 2) * 2 * 25)
				: new ISpecialArmor.ArmorProperties(0, 0.05, (armor.getMaxDamage() - armor.getItemDamage() + 2) / 2 * 25);
		}
	}

	@Override
	public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot)
	{
		if (!hazmatAbsorbs(source) || !hasCompleteHazmat(entity))
		{
			int damageTotal = damage * 2;
			if (this.armorType == EntityEquipmentSlot.FEET && source == DamageSource.FALL)
			{
				damageTotal = (damage + 1) / 2;
			}

			stack.damageItem(damageTotal, entity);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public void onEntityLivingFallEvent(LivingFallEvent event)
	{
		if (IC2.platform.isSimulating() && event.getEntity() instanceof EntityPlayer)
		{
			EntityPlayer player = (EntityPlayer) event.getEntity();
			ItemStack armor = (ItemStack) player.inventory.armorInventory.get(0);
			if (armor != null && armor.getItem() == this)
			{
				int fallDamage = (int) event.getDistance() - 3;
				if (fallDamage >= 8)
				{
					return;
				}

				int armorDamage = (fallDamage + 1) / 2;
				if (armorDamage <= armor.getMaxDamage() - armor.getItemDamage() && armorDamage >= 0)
				{
					armor.damageItem(armorDamage, player);
					event.setCanceled(true);
				}
			}
		}
	}

	public boolean isRepairable()
	{
		return true;
	}

	@Override
	public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot)
	{
		return 1;
	}

	public void onArmorTick(World world, EntityPlayer player, ItemStack stack)
	{
		if (!world.isRemote && this.armorType == EntityEquipmentSlot.HEAD)
		{
			if (player.isBurning() && hasCompleteHazmat(player))
			{
				if (this.isInLava(player))
				{
					player.addPotionEffect(new PotionEffect(MobEffects.FIRE_RESISTANCE, 20, 0, true, true));
				}

				player.extinguish();
			}

			int maxAir = 300;
			int refillThreshold = 100;
			int airToMbMul = 1000;
			int airToMbDiv = 150;
			int minAmount = 7;
			int air = player.getAir();
			if (air <= 100)
			{
				int needed = (300 - air) * 1000 / 150;
				int supplied = 0;

				for (int i = 0; i < player.inventory.mainInventory.size() && needed > 0; i++)
				{
					ItemStack cStack = (ItemStack) player.inventory.mainInventory.get(i);
					if (cStack != null)
					{
						LiquidUtil.FluidOperationResult result = LiquidUtil.drainContainer(
							cStack, FluidName.air.getInstance(), needed, FluidContainerOutputMode.InPlacePreferred
						);
						if (result != null
							&& result.fluidChange.amount >= 7
							&& (result.extraOutput == null || StackUtil.storeInventoryItem(result.extraOutput, player, false)))
						{
							player.inventory.mainInventory.set(i, result.inPlaceOutput);
							int amount = result.fluidChange.amount;
							supplied += amount;
							needed -= amount;
						}
					}
				}

				player.setAir(air + supplied * 150 / 1000);
			}
		}
	}

	public boolean isInLava(EntityPlayer player)
	{
		int x = (int) Math.floor(player.posX);
		int y = (int) Math.floor(player.posY + 0.02);
		int z = (int) Math.floor(player.posZ);
		IBlockState state = player.getEntityWorld().getBlockState(new BlockPos(x, y, z));
		if (state.getBlock() instanceof BlockLiquid
			&& (state.getMaterial() == Material.LAVA || state.getMaterial() == Material.FIRE))
		{
			float height = y + 1 - BlockLiquid.getLiquidHeightPercent((Integer) state.getValue(BlockLiquid.LEVEL));
			return player.posY < height;
		} else
		{
			return false;
		}
	}

	@Override
	public boolean addsProtection(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack)
	{
		return true;
	}

	public static boolean hasCompleteHazmat(EntityLivingBase living)
	{
		for (EntityEquipmentSlot slot : ArmorSlot.getAll())
		{
			ItemStack stack = living.getItemStackFromSlot(slot);
			if (stack == null || !(stack.getItem() instanceof IHazmatLike))
			{
				return false;
			}

			IHazmatLike hazmat = (IHazmatLike) stack.getItem();
			if (!hazmat.addsProtection(living, slot, stack))
			{
				return false;
			}

			if (hazmat.fullyProtects(living, slot, stack))
			{
				return true;
			}
		}

		return true;
	}

	public static boolean hazmatAbsorbs(DamageSource source)
	{
		return source == DamageSource.IN_FIRE
			|| source == DamageSource.IN_WALL
			|| source == DamageSource.LAVA
			|| source == DamageSource.HOT_FLOOR
			|| source == DamageSource.ON_FIRE
			|| source == IC2DamageSource.electricity
			|| source == IC2DamageSource.radiation;
	}

	@Override
	public boolean isMetalArmor(ItemStack itemstack, EntityPlayer player)
	{
		return false;
	}
}
