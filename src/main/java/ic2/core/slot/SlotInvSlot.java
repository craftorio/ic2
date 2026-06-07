package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class SlotInvSlot extends Slot
{
	public final InvSlot invSlot;
	public final int index;

	public SlotInvSlot(InvSlot invSlot, int index, int x, int y)
	{
		super((Container) invSlot.base.getParent(), invSlot.base.getBaseIndex(invSlot) + index, x, y);
		this.invSlot = invSlot;
		this.index = index;
	}

	public boolean m_5857_(ItemStack stack)
	{
		return this.invSlot.accepts(stack);
	}

	public ItemStack m_7993_()
	{
		return this.invSlot.get(this.index);
	}

	public void m_5852_(ItemStack stack)
	{
		this.invSlot.put(this.index, stack);
		this.m_6654_();
	}

	public ItemStack m_6201_(int amount)
	{
		if (amount <= 0)
		{
			return StackUtil.emptyStack;
		}

		ItemStack stack = this.invSlot.get(this.index);
		if (StackUtil.isEmpty(stack))
		{
			return StackUtil.emptyStack;
		}

		amount = Math.min(amount, StackUtil.getSize(stack));
		ItemStack ret;
		if (StackUtil.getSize(stack) == amount)
		{
			ret = stack;
			this.invSlot.clear(this.index);
		} else
		{
			ret = StackUtil.copyWithSize(stack, amount);
			this.invSlot.put(this.index, StackUtil.decSize(stack, amount));
		}

		this.m_6654_();
		return ret;
	}

	public int m_6641_()
	{
		return this.invSlot.getStackSizeLimit();
	}

	public void m_142406_(Player player, ItemStack stack)
	{
		super.m_142406_(player, stack);
		this.invSlot.onPickupFromSlot(player, stack);
	}
}
