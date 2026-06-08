package ic2.core.slot;

import ic2.api.item.ItemWrapper;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotBoxable extends Slot
{
	public SlotBoxable(Container iinventory, int i, int j, int k)
	{
		super(iinventory, i, j, k);
	}

	public boolean mayPlace(ItemStack itemstack)
	{
		return itemstack == null ? false : ItemWrapper.canBeStoredInToolbox(itemstack);
	}
}
