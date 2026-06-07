package ic2.core.slot;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotRadioactive extends Slot
{
	public SlotRadioactive(Container inventory, int index, int x, int y)
	{
		super(inventory, index, x, y);
	}

	public boolean m_5857_(ItemStack stack)
	{
		return this.f_40218_.canPlaceItem(this.m_150661_(), stack);
	}
}
