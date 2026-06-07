package ic2.core.item.armor;

import ic2.api.item.IMetalArmor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item.Properties;

public class ItemArmorIC2 extends ArmorItem implements IMetalArmor
{
	public ItemArmorIC2(ArmorMaterial material, EquipmentSlot slot, Properties settings)
	{
		super(material, slot, settings);
	}

	@Override
	public boolean isMetalArmor(ItemStack itemstack, Player player)
	{
		return true;
	}
}
