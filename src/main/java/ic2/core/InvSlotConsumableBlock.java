package ic2.core;

import ic2.core.block.TileEntityInventory;
import ic2.core.block.invslot.InvSlot;
import ic2.core.block.invslot.InvSlotConsumable;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

public class InvSlotConsumableBlock extends InvSlotConsumable
{
	public InvSlotConsumableBlock(TileEntityInventory base1, String name1, int count)
	{
		this(base1, name1, InvSlot.Access.I, count, InvSlot.InvSide.TOP);
	}

	public InvSlotConsumableBlock(TileEntityInventory base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1)
	{
		super(base1, name1, access1, count, preferredSide1);
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		return stack.getItem() instanceof ItemBlock;
	}
}
