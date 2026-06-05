package ic2.api.recipe;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fluids.Fluid;

public interface IRecipeInputFactory {
   IRecipeInput forStack(ItemStack var1);

   IRecipeInput forStack(ItemStack var1, int var2);

   IRecipeInput forExactStack(ItemStack var1);

   IRecipeInput forExactStack(ItemStack var1, int var2);

   IRecipeInput forOreDict(String var1);

   IRecipeInput forOreDict(String var1, int var2);

   IRecipeInput forOreDict(String var1, int var2, int var3);

   IRecipeInput forFluidContainer(Fluid var1);

   IRecipeInput forFluidContainer(Fluid var1, int var2);

   IRecipeInput forAny(IRecipeInput... var1);

   IRecipeInput forAny(Iterable<IRecipeInput> var1);

   IRecipeInput forIngredient(Ingredient var1);

   Ingredient getIngredient(IRecipeInput var1);
}
