package ic2.core.item;

import ic2.core.IC2;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class DamageHandler
{
	public static int getDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		return item == null ? 0 : stack.getDamageValue();
	}

	public static void setDamage(ItemStack stack, int damage, boolean displayOnly)
	{
		Item item = stack.getItem();
		if (item != null)
		{
			if (item instanceof IPseudoDamageItem)
			{
				if (!displayOnly)
				{
					throw new IllegalStateException("can't damage " + stack + " physically");
				}

				((IPseudoDamageItem) item).setStackDamage(stack, damage);
			} else if (stack.isDamageableItem())
			{
				stack.setDamageValue(damage);
			}
		}
	}

	public static int getMaxDamage(ItemStack stack)
	{
		Item item = stack.getItem();
		return item == null ? 0 : stack.getMaxDamage();
	}

	public static boolean damage(ItemStack stack, int damage, LivingEntity src, InteractionHand hand)
	{
		Item item = stack.getItem();
		if (item == null)
		{
			return false;
		} else if (src != null)
		{
			stack.hurtAndBreak(damage, src, player ->
			{
				if (hand != null)
				{
					player.onEquippedItemBroken(hand);
				}
			});
			return true;
		} else
		{
			return stack.hurt(damage, IC2.random, src instanceof ServerPlayer ? (ServerPlayer) src : null);
		}
	}
}
