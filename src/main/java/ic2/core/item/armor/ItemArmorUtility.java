package ic2.core.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorUtility extends ArmorItem
{
	public ItemArmorUtility(ArmorMaterial material, Properties settings, EquipmentSlot slot)
	{
		super(material, fromSlot(slot), settings);
	}

	private static ArmorItem.Type fromSlot(EquipmentSlot slot)
	{
		return switch (slot)
		{
			case HEAD -> ArmorItem.Type.HELMET;
			case CHEST -> ArmorItem.Type.CHESTPLATE;
			case LEGS -> ArmorItem.Type.LEGGINGS;
			case FEET -> ArmorItem.Type.BOOTS;
			default -> throw new IllegalArgumentException("Invalid slot: " + slot);
		};
	}
}
