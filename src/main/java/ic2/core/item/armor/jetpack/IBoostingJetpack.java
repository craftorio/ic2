package ic2.core.item.armor.jetpack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public interface IBoostingJetpack extends IJetpack
{
	float getBaseThrust(ItemStack stack, boolean hoverMode);

	float getBoostThrust(Player player, ItemStack stack, boolean hoverMode);

	boolean useBoostPower(ItemStack stack, float amount);

	float getHoverBoost(Player player, ItemStack stack, boolean upwards);
}
