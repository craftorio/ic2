package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

public class ItemNanoSaber extends AbstractItemNanoSaber
{
	public ItemNanoSaber(Properties settings)
	{
		super(settings);
	}

	public ItemAttributeModifiers getAttributeModifiers(ItemStack stack, EquipmentSlot slot)
	{
		if (slot != EquipmentSlot.MAINHAND)
		{
			return this.getDefaultAttributeModifiers();
		}

		int dmg = 4;
		float speed = -3.0f;

		if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack))
		{
			dmg = 20;
			speed = 0f;
		}

		ItemAttributeModifiers defaults = this.getDefaultAttributeModifiers();
		ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
		EquipmentSlotGroup eSlotGroup = EquipmentSlotGroup.MAINHAND;

		defaults.forEach(slot, (attribute, modifier) -> {
			if (!attribute.equals(Attributes.ATTACK_DAMAGE) && !attribute.equals(Attributes.ATTACK_SPEED))
			{
				builder.add(attribute, modifier, eSlotGroup);
			}
		});

		builder.add(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_ID, dmg, AttributeModifier.Operation.ADD_VALUE), eSlotGroup);
		builder.add(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_ID, speed, AttributeModifier.Operation.ADD_VALUE), eSlotGroup);
		return builder.build();
	}

	public ItemAttributeModifiers getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		return this.getAttributeModifiers(stack, slot);
	}
}
