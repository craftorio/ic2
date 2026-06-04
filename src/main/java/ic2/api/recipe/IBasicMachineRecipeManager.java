package ic2.api.recipe;

import java.util.Collection;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IBasicMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack>
{
	boolean addRecipe(IRecipeInput paramIRecipeInput, NBTTagCompound paramNBTTagCompound, boolean paramBoolean, ItemStack... paramVarArgs);

	@Deprecated
	RecipeOutput getOutputFor(ItemStack paramItemStack, boolean paramBoolean);
}
