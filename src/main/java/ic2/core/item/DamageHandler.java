package ic2.core.item;

import ic2.api.item.ICustomDamageItem;
import ic2.core.IC2;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class DamageHandler
{
	public static int getDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return 0;
		} else
		{
			return item instanceof ICustomDamageItem ? ((ICustomDamageItem) item).getCustomDamage(stack) : stack.getItemDamage();
		}
	}

	public static void setDamage(ItemStack stack, int damage, boolean displayOnly)
	{
		Item item = stack.getItem();
		if (item != null)
		{
			if (item instanceof ICustomDamageItem)
			{
				((ICustomDamageItem) item).setCustomDamage(stack, damage);
			} else if (item instanceof IPseudoDamageItem)
			{
				if (!displayOnly)
				{
					throw new IllegalStateException("can't damage " + stack + " physically");
				}

				((IPseudoDamageItem) item).setStackDamage(stack, damage);
			} else
			{
				stack.setItemDamage(damage);
			}
		}
	}

	public static int getMaxDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return 0;
		} else
		{
			return item instanceof ICustomDamageItem ? ((ICustomDamageItem) item).getMaxCustomDamage(stack) : stack.getMaxDamage();
		}
	}

	public static boolean damage(ItemStack stack, int damage, EntityLivingBase src)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return false;
		} else if (item instanceof ICustomDamageItem)
		{
			return ((ICustomDamageItem) item).applyCustomDamage(stack, damage, src);
		} else if (src != null)
		{
			stack.damageItem(damage, src);
			return true;
		} else
		{
			return stack.attemptDamageItem(damage, IC2.random, src instanceof EntityPlayerMP ? (EntityPlayerMP) src : null);
		}
	}
}
