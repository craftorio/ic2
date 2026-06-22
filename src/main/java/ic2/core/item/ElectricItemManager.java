package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.core.IC2;
import ic2.core.slot.ArmorSlot;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;

import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ElectricItemManager implements IElectricItemManager
{
	private static int mapChargeLevelToDamage(double charge, double maxCharge, int maxDamage)
	{
		if (maxDamage < 2)
		{
			return 0;
		}

		maxDamage--;
		return maxDamage - (int) Util.map(charge, maxCharge, maxDamage);
	}

	public static ItemStack getCharged(Item item, double charge)
	{
		if (!(item instanceof IElectricItem))
		{
			throw new IllegalArgumentException("no electric item");
		}

		ItemStack ret = new ItemStack(item);
		ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
		return ret;
	}

	public static void addChargeVariants(Item item, List<ItemStack> list)
	{
		list.add(getCharged(item, 0.0));
		list.add(getCharged(item, Double.POSITIVE_INFINITY));
	}

	@Override
	public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate)
	{
		IElectricItem item = (IElectricItem) stack.getItem();
		assert item.getMaxCharge(stack) > 0.0;
		if (!(amount < 0.0) && StackUtil.getSize(stack) <= 1 && item.getTier(stack) <= tier)
		{
			if (!ignoreTransferLimit && amount > item.getTransferLimit(stack))
			{
				amount = item.getTransferLimit(stack);
			}

			CompoundTag tNBT = StackUtil.getOrCreateNbtData(stack);
			double newCharge = tNBT.getDouble("charge");
			amount = Math.min(amount, item.getMaxCharge(stack) - newCharge);
			if (!simulate)
			{
				newCharge += amount;
				if (newCharge > 0.0)
				{
					tNBT.putDouble("charge", newCharge);
				} else
				{
					tNBT.remove("charge");
					if (tNBT.isEmpty())
					{
						stack.setTag(null);
					}
				}

				if (stack.getItem() instanceof IElectricItem)
				{
					item = (IElectricItem) stack.getItem();
					int maxDamage = DamageHandler.getMaxDamage(stack);
					DamageHandler.setDamage(stack, mapChargeLevelToDamage(newCharge, item.getMaxCharge(stack), maxDamage), true);
				} else
				{
					DamageHandler.setDamage(stack, 0, true);
				}
			}

			return amount;
		} else
		{
			return 0.0;
		}
	}

	@Override
	public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate)
	{
		IElectricItem item = (IElectricItem) stack.getItem();
		assert item.getMaxCharge(stack) > 0.0;
		if (amount < 0.0 || StackUtil.getSize(stack) > 1 || item.getTier(stack) > tier)
		{
			return 0.0;
		}

		if (externally && !item.canProvideEnergy(stack))
		{
			return 0.0;
		}

		if (!ignoreTransferLimit && amount > item.getTransferLimit(stack))
		{
			amount = item.getTransferLimit(stack);
		}

		CompoundTag tNBT = StackUtil.getOrCreateNbtData(stack);
		double newCharge = tNBT.getDouble("charge");
		amount = Math.min(amount, newCharge);
		if (!simulate)
		{
			newCharge -= amount;
			if (newCharge > 0.0)
			{
				tNBT.putDouble("charge", newCharge);
			} else
			{
				tNBT.remove("charge");
				if (tNBT.isEmpty())
				{
					stack.setTag(null);
				}
			}

			if (stack.getItem() instanceof IElectricItem)
			{
				item = (IElectricItem) stack.getItem();
				int maxDamage = DamageHandler.getMaxDamage(stack);
				DamageHandler.setDamage(stack, mapChargeLevelToDamage(newCharge, item.getMaxCharge(stack), maxDamage), true);
			} else
			{
				DamageHandler.setDamage(stack, 0, true);
			}
		}

		return amount;
	}

	@Override
	public double getCharge(ItemStack stack)
	{
		return ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, true);
	}

	@Override
	public double getStackCharge(ItemStack stack)
	{
		IElectricItem item = (IElectricItem) stack.getItem();
		assert item.getMaxCharge(stack) > 0.0;
		CompoundTag tNBT = StackUtil.getOrCreateNbtData(stack);
		return tNBT.getDouble("charge");
	}

	@Override
	public double getMaxCharge(ItemStack stack)
	{
		return ElectricItem.manager.getCharge(stack) + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
	}

	@Override
	public boolean canUse(ItemStack stack, double amount)
	{
		return ElectricItem.manager.getCharge(stack) >= amount;
	}

	@Override
	public boolean use(ItemStack stack, double amount, LivingEntity entity)
	{
		if (entity != null)
		{
			ElectricItem.manager.chargeFromArmor(stack, entity);
		}

		double transfer = ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, true);
		if (Util.isSimilar(transfer, amount))
		{
			ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, false);
			if (entity != null)
			{
				ElectricItem.manager.chargeFromArmor(stack, entity);
			}

			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public void chargeFromArmor(ItemStack target, LivingEntity entity)
	{
		boolean transferred = false;

		for (EquipmentSlot slot : ArmorSlot.getAll())
		{
			ItemStack source = entity.getItemBySlot(slot);
			if (source != null)
			{
				int tier;
				if (source.getItem() instanceof IElectricItem)
				{
					tier = ((IElectricItem) source.getItem()).getTier(target);
				} else
				{
					tier = Integer.MAX_VALUE;
				}

				double transfer = ElectricItem.manager.discharge(source, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true);
				if (!(transfer <= 0.0))
				{
					transfer = ElectricItem.manager.charge(target, transfer, tier, true, false);
					if (!(transfer <= 0.0))
					{
						ElectricItem.manager.discharge(source, transfer, Integer.MAX_VALUE, true, true, false);
						transferred = true;
					}
				}
			}
		}

		if (transferred && entity instanceof Player && IC2.sideProxy.isSimulating())
		{
			((Player) entity).containerMenu.broadcastChanges();
		}
	}

	@Override
	public String getToolTip(ItemStack stack)
	{
		double charge = ElectricItem.manager.getCharge(stack);
		double space = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
		return Util.toSiString(charge, 3) + "/" + Util.toSiString(charge + space, 3) + " EU";
	}

	@Override
	public int getTier(ItemStack stack)
	{
		return stack != null && stack.getItem() instanceof IElectricItem ? ((IElectricItem) stack.getItem()).getTier(stack) : 0;
	}
}
