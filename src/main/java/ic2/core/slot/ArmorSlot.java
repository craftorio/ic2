package ic2.core.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;

public class ArmorSlot
{
	private static final EntityEquipmentSlot[] armorSlots = getArmorSlots();
	private static final List<EntityEquipmentSlot> armorSlotList = Collections.unmodifiableList(Arrays.asList(armorSlots));

	public static EntityEquipmentSlot get(int index)
	{
		return armorSlots[index];
	}

	public static int getCount()
	{
		return armorSlots.length;
	}

	public static Iterable<EntityEquipmentSlot> getAll()
	{
		return armorSlotList;
	}

	private static EntityEquipmentSlot[] getArmorSlots()
	{
		EntityEquipmentSlot[] values = EntityEquipmentSlot.values();
		int count = 0;

		for (EntityEquipmentSlot slot : values)
		{
			if (slot.getSlotType() == Type.ARMOR)
			{
				count++;
			}
		}

		EntityEquipmentSlot[] ret = new EntityEquipmentSlot[count];

		for (int i = 0; i < ret.length; i++)
		{
			for (EntityEquipmentSlot slot : values)
			{
				if (slot.getSlotType() == Type.ARMOR && slot.getIndex() == i)
				{
					ret[i] = slot;
					break;
				}
			}
		}

		for (int i = 0; i < ret.length; i++)
		{
			if (ret[i] == null)
			{
				throw new RuntimeException("Can't find an armor mapping for idx " + i);
			}
		}

		return ret;
	}
}
