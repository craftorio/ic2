package ic2.core.item;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public interface PriorityUsableItem
{
	InteractionResult onItemUseFirst(ItemStack var1, UseOnContext var2);
}
