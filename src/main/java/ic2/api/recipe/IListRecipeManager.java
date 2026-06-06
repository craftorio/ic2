package ic2.api.recipe;

import java.util.List;

import net.minecraft.item.ItemStack;

public interface IListRecipeManager extends Iterable<IRecipeInput>
{
	void add(IRecipeInput var1);

	boolean contains(ItemStack var1);

	boolean isEmpty();

	List<IRecipeInput> getInputs();
}
