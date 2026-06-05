package ic2.core.item;

import ic2.core.IHasGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IHandHeldInventory {
   IHasGui getInventory(EntityPlayer var1, ItemStack var2);
}
