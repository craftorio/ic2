package ic2.core.util;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class ItemComparableItemStack
{
	private final Item item;
	private final int meta;
	private final NBTTagCompound nbt;
	private final int hashCode;

	public ItemComparableItemStack(ItemStack stack, boolean copyNbt)
	{
		this.item = stack.getItem();
		this.meta = stack.getHasSubtypes() ? stack.getMetadata() : 0;
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt != null)
		{
			if (nbt.hasNoTags())
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
					if (!copied && nbt.hasKey(key))
					{
						nbt = nbt.copy();
						copied = true;
					}

					nbt.removeTag(key);
				}

				if (nbt.hasNoTags())
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
		this.meta = src.meta;
		this.nbt = src.nbt != null ? src.nbt.copy() : null;
		this.hashCode = src.hashCode;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ItemComparableItemStack))
		{
			return false;
		} else
		{
			ItemComparableItemStack cmp = (ItemComparableItemStack) obj;
			if (cmp.hashCode != this.hashCode)
			{
				return false;
			} else
			{
				return cmp == this
					? true
					: cmp.item == this.item
					  && cmp.meta == this.meta
					  && (cmp.nbt == null && this.nbt == null || cmp.nbt != null && this.nbt != null && cmp.nbt.equals(this.nbt));
			}
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

		ret = ret * 31 + this.meta;
		if (this.nbt != null)
		{
			ret = ret * 61 + this.nbt.hashCode();
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

		ItemStack ret = new ItemStack(this.item, size, this.meta);
		ret.setTagCompound(this.nbt);
		return ret;
	}
}
