package ic2.api.recipe;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

@Deprecated
public final class RecipeOutput
{
	public final List<ItemStack> items;
	public final CompoundTag metadata;

	public RecipeOutput(CompoundTag metadata1, List<ItemStack> items1)
	{
		assert !items1.contains(null);
		this.metadata = metadata1;
		this.items = items1;
	}

	public RecipeOutput(CompoundTag metadata1, ItemStack... items1)
	{
		this(metadata1, Arrays.asList(items1));
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof RecipeOutput ro
			&& this.items.size() == ro.items.size()
			&& (this.metadata == null && ro.metadata == null || this.metadata != null && ro.metadata != null && this.metadata.equals(ro.metadata)))
		{
			Iterator<ItemStack> itA = this.items.iterator();
			Iterator<ItemStack> itB = ro.items.iterator();

			while (itA.hasNext() && itB.hasNext())
			{
				ItemStack stackA = itA.next();
				ItemStack stackB = itB.next();
				if (ItemStack.matches(stackA, stackB))
				{
					return false;
				}
			}

			return true;
		} else
		{
			return false;
		}
	}

	@Override
	public String toString()
	{
		return "ROutput<" + this.items + "," + this.metadata + ">";
	}
}
