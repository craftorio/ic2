package ic2.api.item;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot.Type;
import net.minecraft.world.item.ItemStack;

public interface IHazmatLike
{
	static boolean hasCompleteHazmat(LivingEntity living)
	{
		for (EquipmentSlot slot : EquipmentSlot.values())
		{
			if (slot.getType() == Type.ARMOR)
			{
				ItemStack stack = living.getItemBySlot(slot);
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
		}

		return true;
	}

	boolean addsProtection(LivingEntity var1, EquipmentSlot var2, ItemStack var3);

	default boolean fullyProtects(LivingEntity entity, EquipmentSlot slot, ItemStack stack)
	{
		return false;
	}
}
