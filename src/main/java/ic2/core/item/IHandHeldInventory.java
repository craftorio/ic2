package ic2.core.item;

import ic2.core.IHasGui;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public interface IHandHeldInventory {
  IHasGui getInventory(EntityPlayer paramEntityPlayer, ItemStack paramItemStack);
}
