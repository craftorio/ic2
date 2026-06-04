package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableId extends InvSlotConsumable
{
	private final Set<Item> items;

	public InvSlotConsumableId(IInventorySlotHolder<?> base1, String name1, int count, Item... items)
	{
		this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP, items);
	}

	public InvSlotConsumableId(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, Item... items)
	{
		super(base1, name1, access1, count, preferredSide1);
		this.items = new HashSet<>();
		this.items.addAll(Arrays.asList(items));
	}

	public boolean accepts(ItemStack stack)
	{
		return this.items.contains(stack.getItem());
	}
}
