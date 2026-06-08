package ic2.core.item.tool;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.ImmutableMultimap.Builder;
import ic2.api.item.ElectricItem;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemNanoSaber extends AbstractItemNanoSaber
{
	public ItemNanoSaber(Properties settings)
	{
		super(settings);
	}

	public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack)
	{
		if (slot != EquipmentSlot.MAINHAND)
		{
			return this.getDefaultAttributeModifiers(slot);
		}

		int dmg = 4;
		if (ElectricItem.manager.canUse(stack, 400.0) && isActive(stack))
		{
			dmg = 20;
		}

		Multimap<String, AttributeModifier> ret = HashMultimap.create();
		Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
		builder.put(Attributes.ATTACK_DAMAGE, new AttributeModifier(BASE_ATTACK_DAMAGE_UUID, "Tool modifier", dmg, Operation.ADDITION));
		builder.put(Attributes.ATTACK_SPEED, new AttributeModifier(BASE_ATTACK_SPEED_UUID, "Tool modifier", -3.0, Operation.ADDITION));
		return builder.build();
	}
}
