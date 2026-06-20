package ic2.core.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.component.DataComponents;

public class ItemComparableItemStack
{
	private final Item item;
	private final CompoundTag nbt;
	private final int hashCode;

	public ItemComparableItemStack(ItemStack stack, boolean copyNbt)
	{
		this.item = stack.getItem();
		CompoundTag nbt = stack.getTag();
		if (nbt != null)
		{
			if (nbt.isEmpty())
			{
				nbt = null;
			} else
			{
				if (copyNbt)
				{
					nbt = nbt.copy();
				}

				boolean copied = copyNbt;

				for (String key : StackUtil.ignoredNbtKeys)
				{
					if (!copied && nbt.contains(key))
					{
						nbt = nbt.copy();
						copied = true;
					}

					nbt.remove(key);
				}

				if (nbt.isEmpty())
				{
					nbt = null;
				}
			}
		}

		this.nbt = nbt;
		this.hashCode = this.calculateHashCode();
	}

	private ItemComparableItemStack(ItemComparableItemStack src)
	{
		this.item = src.item;
		this.nbt = src.nbt != null ? src.nbt.copy() : null;
		this.hashCode = src.hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ItemComparableItemStack cmp))
		{
			return false;
		} else if (cmp.hashCode != this.hashCode)
		{
			return false;
		} else
		{
			return cmp == this
				? true
				: cmp.item == this.item && (cmp.nbt == null && this.nbt == null || cmp.nbt != null && this.nbt != null && cmp.nbt.equals(this.nbt));
		}
	}

	@Override
	public int hashCode()
	{
		return this.hashCode;
	}

	private int calculateHashCode()
	{
		int ret = 0;
		if (this.item != null)
		{
			ret = System.identityHashCode(this.item);
		}

		if (this.nbt != null)
		{
			ret = ret * 31 + this.nbt.hashCode();
		}

		return ret;
	}

	public ItemComparableItemStack copy()
	{
		return this.nbt == null ? this : new ItemComparableItemStack(this);
	}

	public ItemStack toStack()
	{
		return this.toStack(1);
	}

	public ItemStack toStack(int size)
	{
		if (this.item == null)
		{
			return null;
		}

		ItemStack ret = new ItemStack(this.item, size);
		ret.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA, net.minecraft.world.item.component.CustomData.of(this.nbt));
		return ret;
	}
}
