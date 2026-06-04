package ic2.api.item;

import net.minecraft.item.ItemStack;

public interface IItemHudProvider
{
	boolean doesProvideHUD(ItemStack paramItemStack);

	HudMode getHudMode(ItemStack paramItemStack);

	interface IItemHudBarProvider
	{
		int getBarPercent(ItemStack param1ItemStack);
	}
}
