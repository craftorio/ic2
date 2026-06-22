package ic2.core.slot;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class SlotInvSlot extends Slot
{
	public final InvSlot invSlot;
	public final int index;

	public SlotInvSlot(InvSlot invSlot, int index, int x, int y)
	{
		super(invSlot.base.getParent(), invSlot.base.getBaseIndex(invSlot) + index, x, y);
		this.invSlot = invSlot;
		this.index = index;
	}

	public boolean mayPlace(ItemStack stack)
	{
		return this.invSlot.accepts(stack);
	}

	public @NotNull ItemStack getItem()
	{
		return this.invSlot.get(this.index);
	}

	public void set(@NotNull ItemStack stack)
	{
		this.invSlot.put(this.index, stack);
		this.setChanged();
	}

	public ItemStack remove(int amount)
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

		this.setChanged();
		return ret;
	}

	public int getMaxStackSize()
	{
		return this.invSlot.getStackSizeLimit();
	}

	public void onTake(Player player, ItemStack stack)
	{
		super.onTake(player, stack);
		this.invSlot.onPickupFromSlot();
	}
}
