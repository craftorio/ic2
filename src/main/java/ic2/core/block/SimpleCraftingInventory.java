package ic2.core.block;

import ic2.core.block.invslot.InvSlot;
import ic2.core.util.StackUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;

public abstract class SimpleCraftingInventory implements CraftingContainer
{
	private final int width;
	private final int height;
	private final int size;

	public SimpleCraftingInventory(int width, int height)
	{
		super();
		this.width = width;
		this.height = height;
		this.size = width * height;
	}

	@Override
	public int getWidth()
	{
		return this.width;
	}

	@Override
	public int getHeight()
	{
		return this.height;
	}

	public ItemStack getItem(int index)
	{
		return index >= this.size ? StackUtil.emptyStack : StackUtil.wrapEmpty(this.get(index));
	}

	public void setItem(int index, ItemStack stack)
	{
		this.set(index, stack);
	}

	public ItemStack removeItemNoUpdate(int index)
	{
		ItemStack ret;
		if (index < this.size && !StackUtil.isEmpty(ret = this.get(index)))
		{
			this.set(index, StackUtil.emptyStack);
			return ret;
		} else
		{
			return StackUtil.emptyStack;
		}
	}

	public ItemStack removeItem(int index, int amount)
	{
		ItemStack stack;
		return index < this.size && amount > 0 && !StackUtil.isEmpty(stack = this.get(index)) ? stack.split(amount) : StackUtil.emptyStack;
	}

	public boolean isEmpty()
	{
		for (int i = 0; i < this.size; i++)
		{
			if (!StackUtil.isEmpty(this.get(i)))
			{
				return false;
			}
		}

		return true;
	}

	public void clearContent()
	{
		for (int i = 0; i < this.size; i++)
		{
			this.set(i, StackUtil.emptyStack);
		}
	}

	public void fillStackedContents(StackedContents finder)
	{
		for (int i = 0; i < this.size; i++)
		{
			ItemStack stack = this.get(i);
			if (!StackUtil.isEmpty(stack))
			{
				finder.accountSimpleStack(stack);
			}
		}
	}

	@Override
	public NonNullList<ItemStack> getItems()
	{
		NonNullList<ItemStack> list = NonNullList.withSize(this.size, ItemStack.EMPTY);
		for (int i = 0; i < this.size; i++)
		{
			list.set(i, this.getItem(i));
		}
		return list;
	}

	@Override
	public int getContainerSize()
	{
		return this.size;
	}

	@Override
	public boolean stillValid(Player player)
	{
		return true;
	}

	@Override
	public void setChanged()
	{
	}

	protected abstract ItemStack get(int var1);

	protected abstract void set(int var1, ItemStack var2);

	public static class ArrayCraftingInventory extends SimpleCraftingInventory
	{
		private final ItemStack[] items;

		public ArrayCraftingInventory(ItemStack[] items, int width)
		{
			super(width, (items.length + width - 1) / width);
			this.items = items;
		}

		@Override
		protected ItemStack get(int index)
		{
			return this.items[index];
		}

		@Override
		protected void set(int index, ItemStack stack)
		{
			this.items[index] = stack;
		}
	}

	public static class InvSlotCraftingInventory extends SimpleCraftingInventory
	{
		private final InvSlot invSlot;

		public InvSlotCraftingInventory(InvSlot invSlot, int width)
		{
			super(width, (invSlot.size() + width - 1) / width);
			this.invSlot = invSlot;
		}

		@Override
		protected ItemStack get(int index)
		{
			return this.invSlot.get(index);
		}

		@Override
		protected void set(int index, ItemStack stack)
		{
			this.invSlot.put(index, stack);
		}
	}
}
