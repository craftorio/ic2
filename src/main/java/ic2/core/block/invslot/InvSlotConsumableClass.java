package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

public class InvSlotConsumableClass extends InvSlotConsumable
{
	private final Class<?> clazz;

	public InvSlotConsumableClass(IInventorySlotHolder<?> base1, String name1, InvSlot.Access access1, int count, InvSlot.InvSide preferredSide1, Class<?> clazz)
	{
		super(base1, name1, access1, count, preferredSide1);
		this.clazz = clazz;
	}

	public InvSlotConsumableClass(IInventorySlotHolder<?> base1, String name1, int count, Class<?> clazz)
	{
		super(base1, name1, count);
		this.clazz = clazz;
	}

	@Override
	public boolean accepts(ItemStack stack)
	{
		if (StackUtil.isEmpty(stack))
		{
			return false;
		} else
		{
			return stack.getItem() instanceof BlockItem ? this.clazz.isInstance(Block.byItem(stack.getItem())) : this.clazz.isInstance(stack.getItem());
		}
	}
}
