package ic2.core.slot;

import com.mojang.datafixers.util.Pair;
import ic2.core.util.ReflectionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SlotArmor extends Slot
{
	private static final ResourceLocation[] EMPTY_ARMOR_SLOT_TEXTURES = ReflectionUtil.getFieldValue(
		ReflectionUtil.getField(InventoryMenu.class, ResourceLocation[].class), null
	);
	private final EquipmentSlot armorType;

	public SlotArmor(Inventory inventory, EquipmentSlot armorType, int x, int y)
	{
		super(inventory, 36 + armorType.m_20749_(), x, y);
		this.armorType = armorType;
	}

	public boolean m_5857_(ItemStack stack)
	{
		Item item = stack.getItem();
		return item == null ? false : Mob.m_147233_(stack) == this.armorType;
	}

	public Pair<ResourceLocation, ResourceLocation> m_7543_()
	{
		return Pair.of(InventoryMenu.f_39692_, EMPTY_ARMOR_SLOT_TEXTURES[this.armorType.m_20749_()]);
	}
}
