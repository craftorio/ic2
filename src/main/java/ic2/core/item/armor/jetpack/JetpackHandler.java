package ic2.core.item.armor.jetpack;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class JetpackHandler
{
	public static void onPlayerTick(Player player)
	{
		ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!chestStack.isEmpty() && chestStack.getItem() instanceof IJetpack jetpack)
		{
			JetpackLogic.onArmorTick(player.level(), player, chestStack, jetpack);
		}
	}
}
