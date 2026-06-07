package ic2.api.item;

import net.minecraft.world.item.ItemStack;

public interface ISpecialElectricItem
{
	IElectricItemManager getManager(ItemStack var1);
}
