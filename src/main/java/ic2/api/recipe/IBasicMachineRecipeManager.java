package ic2.api.recipe;

import java.util.Collection;

import net.minecraft.world.item.ItemStack;

public interface IBasicMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	@Deprecated
	RecipeOutput getOutputFor(ItemStack var1, boolean var2);
}
