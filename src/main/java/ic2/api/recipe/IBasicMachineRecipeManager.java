package ic2.api.recipe;

import java.util.Collection;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IBasicMachineRecipeManager extends IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
   boolean addRecipe(IRecipeInput var1, NBTTagCompound var2, boolean var3, ItemStack... var4);

   @Deprecated
   RecipeOutput getOutputFor(ItemStack var1, boolean var2);
}
