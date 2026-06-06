package ic2.core.item;

import ic2.core.IHasGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IHandHeldSubInventory extends IHandHeldInventory
{
	IHasGui getSubInventory(EntityPlayer var1, ItemStack var2, int var3);
}
