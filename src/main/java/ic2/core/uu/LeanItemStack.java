package ic2.core.uu;

import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LeanItemStack
{
	private final Item item;
	private final CompoundTag nbt;
	private final int size;
	private int hashCode;

	public LeanItemStack(Item item)
	{
		this(item, 1);
	}

	public LeanItemStack(Item item, int size)
	{
		this(item, null, size);
	}

	public LeanItemStack(ItemStack stack)
	{
		this(stack.getItem(), stack.getTag(), StackUtil.getSize(stack));
	}

	public LeanItemStack(ItemStack stack, int size)
	{
		this(stack.getItem(), stack.getTag(), size);
	}

	public LeanItemStack(Item item, CompoundTag nbt, int size)
	{
		if (item == null)
		{
			throw new NullPointerException("null item");
		}

		this.item = item;
		this.nbt = nbt;
		this.size = size;
	}

	public Item getItem()
	{
		return this.item;
	}

	public CompoundTag getNbt()
	{
		return this.nbt;
	}

	public int getSize()
	{
		return this.size;
	}

	@Override
	public String toString()
	{
		return String.format("%dx%s", this.size, Util.getName(this.item));
	}

	public boolean hasSameItem(LeanItemStack o)
	{
		return this.item == o.item && StackUtil.checkNbtEquality(this.nbt, o.nbt);
	}

	public LeanItemStack copy()
	{
		return this.copyWithSize(this.size);
	}

	public LeanItemStack copyWithSize(int newSize)
	{
		LeanItemStack ret = new LeanItemStack(this.item, this.nbt, newSize);
		ret.hashCode = this.hashCode;
		return ret;
	}

	public ItemStack toMcStack()
	{
		if (this.size <= 0)
		{
			return StackUtil.emptyStack;
		}

		ItemStack ret = new ItemStack(this.item, this.size);
		ret.setTag(this.nbt);
		return ret;
	}

	@Override
	public boolean equals(Object obj)
	{
		return !(obj instanceof LeanItemStack o)
			? false
			: this.item == o.item && (this.nbt == null && o.nbt == null || this.nbt != null && o.nbt != null && this.nbt.equals(o.nbt));
	}

	@Override
	public int hashCode()
	{
		if (this.hashCode == 0)
		{
			this.hashCode = this.calculateHashCode();
		}

		return this.hashCode;
	}

	private int calculateHashCode()
	{
		int ret = System.identityHashCode(this.item);
		if (this.nbt != null)
		{
			ret = ret * 31 + this.nbt.hashCode();
		}

		if (ret == 0)
		{
			ret = -1;
		}

		return ret;
	}
}
