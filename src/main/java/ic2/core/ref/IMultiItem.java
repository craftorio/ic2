package ic2.core.ref;

import ic2.core.block.state.IIdProvider;
import ic2.core.util.StackUtil;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.item.ItemStack;

public interface IMultiItem<T extends IIdProvider>
{
	ItemStack getItemStack(T var1);

	ItemStack getItemStack(String var1);

	String getVariant(ItemStack var1);

	Set<T> getAllTypes();

	default Set<ItemStack> getAllStacks()
	{
		Set<ItemStack> ret = new HashSet<>();

		for (T type : this.getAllTypes())
		{
			ret.add(this.getItemStack(type));
		}

		ret.remove(null);
		ret.remove(StackUtil.emptyStack);
		return ret;
	}
}
