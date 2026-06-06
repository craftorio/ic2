package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.ItemComparableItemStack;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

public class InvSlotConsumableItemStack extends InvSlotConsumable
{
	private final Set<ItemComparableItemStack> stacks = new HashSet<>();

	public InvSlotConsumableItemStack(IInventorySlotHolder<?> base1, String name1, int count, ItemStack... stacks)
	{
		this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP, stacks);
	}

	public InvSlotConsumableItemStack(
		IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, ItemStack... stacks
	)
	{
		super(base1, name1, access1, count, preferredSide1);

		for (ItemStack stack : stacks)
		{
			this.stacks.add(new ItemComparableItemStack(stack, true));
		}
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return this.stacks.contains(new ItemComparableItemStack(stack, false));
	}
}
