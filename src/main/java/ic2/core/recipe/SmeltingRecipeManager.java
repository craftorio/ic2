package ic2.core.recipe;

import com.google.common.collect.Iterables;
import ic2.api.recipe.IMachineRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.api.recipe.MachineRecipe;
import ic2.api.recipe.MachineRecipeResult;
import ic2.api.recipe.Recipes;
import ic2.core.util.StackUtil;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;

public class SmeltingRecipeManager implements IMachineRecipeManager<ItemStack, ItemStack, ItemStack> {
  public boolean addRecipe(ItemStack input, ItemStack output, NBTTagCompound metadata, boolean replace) {
    FurnaceRecipes recipes = FurnaceRecipes.func_77602_a();
    if (!StackUtil.isEmpty(recipes.func_151395_a(input)) && !replace)
      return false; 
    float experience = (metadata != null && metadata.func_74764_b("experience")) ? metadata.func_74760_g("experience") : 0.0F;
    if (experience < 0.0F)
      throw new IllegalArgumentException("Negative xp for " + StackUtil.toStringSafe(input) + " -> " + StackUtil.toStringSafe(output)); 
    recipes.func_151394_a(input, output, experience);
    return true;
  }
  
  public MachineRecipeResult<ItemStack, ItemStack, ItemStack> apply(ItemStack input, boolean acceptTest) {
    FurnaceRecipes recipes = FurnaceRecipes.func_77602_a();
    ItemStack output = recipes.func_151395_a(input);
    if (StackUtil.isEmpty(output))
      return null; 
    NBTTagCompound nbt = new NBTTagCompound();
    nbt.func_74776_a("experience", recipes.func_151398_b(output) * StackUtil.getSize(output));
    return (new MachineRecipe(input, output, nbt)).getResult(StackUtil.copyShrunk(input, 1));
  }
  
  public Iterable<? extends MachineRecipe<ItemStack, ItemStack>> getRecipes() {
    throw new UnsupportedOperationException();
  }
  
  public boolean isIterable() {
    return false;
  }
  
  public enum SmeltingBridge implements IMachineRecipeManager<IRecipeInput, Collection<ItemStack>, ItemStack> {
    INSTANCE;
    
    public boolean addRecipe(IRecipeInput input, Collection<ItemStack> output, NBTTagCompound metadata, boolean replace) {
      ItemStack realOutput = (ItemStack)Iterables.getOnlyElement(output);
      boolean ret = false;
      for (ItemStack stack : input.getInputs())
        ret |= Recipes.furnace.addRecipe(stack, realOutput, metadata, replace); 
      return ret;
    }
    
    public MachineRecipeResult<IRecipeInput, Collection<ItemStack>, ItemStack> apply(ItemStack input, boolean acceptTest) {
      MachineRecipeResult<ItemStack, ItemStack, ItemStack> normal = Recipes.furnace.apply(input, acceptTest);
      if (normal == null)
        return null; 
      MachineRecipe<ItemStack, ItemStack> result = normal.getRecipe();
      IRecipeInput resultIn = Recipes.inputFactory.forStack((ItemStack)result.getInput());
      Collection<ItemStack> resultOut = Collections.singletonList(result.getOutput());
      NBTTagCompound resultNBT = result.getMetaData();
      return (new MachineRecipe(resultIn, resultOut, resultNBT)).getResult(normal.getAdjustedInput());
    }
    
    public Iterable<? extends MachineRecipe<IRecipeInput, Collection<ItemStack>>> getRecipes() {
      throw new UnsupportedOperationException();
    }
    
    public boolean isIterable() {
      return false;
    }
  }
}
