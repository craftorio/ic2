package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableLinked extends InvSlotConsumable
{
	public final InvSlot linkedSlot;

	public InvSlotConsumableLinked(IInventorySlotHolder<?> base1, String name1, int count, InvSlot linkedSlot1)
	{
		super(base1, name1, count);
		this.linkedSlot = linkedSlot1;
	}

	public boolean accepts(ItemStack stack)
	{
		ItemStack required = this.linkedSlot.get();
		if (StackUtil.isEmpty(required))
			return false;
		return StackUtil.checkItemEqualityStrict(required, stack);
	}

	public ItemStack consumeLinked(boolean simulate)
	{
		ItemStack required = this.linkedSlot.get();
		if (StackUtil.isEmpty(required))
			return null;
		int reqAmount = StackUtil.getSize(required);
		ItemStack available = consume(reqAmount, true, true);
		if (!StackUtil.isEmpty(available) && StackUtil.getSize(available) == reqAmount)
			return consume(reqAmount, simulate, true);
		return null;
	}
}
