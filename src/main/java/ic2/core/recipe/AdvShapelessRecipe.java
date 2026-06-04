package ic2.core.recipe;

import ic2.api.item.ElectricItem;
import ic2.api.recipe.ICraftingRecipeManager;
import ic2.api.recipe.IRecipeInput;
import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import java.util.List;
import java.util.Vector;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;

public class AdvShapelessRecipe implements IRecipe {
  public ItemStack output;
  
  public IRecipeInput[] input;
  
  public boolean hidden;
  
  public boolean consuming;
  
  private ResourceLocation name;
  
  public static void addAndRegister(ItemStack result, Object... args) {
    try {
      Rezepte.registerRecipe(new AdvShapelessRecipe(result, args));
    } catch (RuntimeException e) {
      if (!MainConfig.ignoreInvalidRecipes)
        throw e; 
    } 
  }
  
  public AdvShapelessRecipe(ItemStack result, Object... args) {
    if (result == null) {
      AdvRecipe.displayError("null result", null, null, true);
    } else {
      result = result.copy();
    } 
    this.input = new IRecipeInput[args.length - Util.countInArray(args, new Class[] { Boolean.class, ICraftingRecipeManager.AttributeContainer.class })];
    int inputIndex = 0;
    for (Object o : args) {
      if (o instanceof Boolean) {
        this.hidden = ((Boolean)o).booleanValue();
      } else if (o instanceof ICraftingRecipeManager.AttributeContainer) {
        this.hidden = ((ICraftingRecipeManager.AttributeContainer)o).hidden;
        this.consuming = ((ICraftingRecipeManager.AttributeContainer)o).consuming;
      } else {
        try {
          this.input[inputIndex++] = AdvRecipe.getRecipeObject(o);
        } catch (Exception e) {
          e.printStackTrace();
          AdvRecipe.displayError("unknown type", "O: " + o + "\nT: " + o.getClass().getName(), result, true);
        } 
      } 
    } 
    if (inputIndex != this.input.length)
      AdvRecipe.displayError("length calculation error", "I: " + inputIndex + "\nL: " + this.input.length, result, true); 
    this.output = result;
  }
  
  public boolean matches(InventoryCrafting inventorycrafting, World world) {
    return (getCraftingResult(inventorycrafting) != StackUtil.emptyStack);
  }
  
  public ItemStack getCraftingResult(InventoryCrafting inventorycrafting) {
    int offerSize = inventorycrafting.getSizeInventory();
    if (offerSize < this.input.length)
      return StackUtil.emptyStack; 
    List<IRecipeInput> unmatched = new Vector<>();
    for (IRecipeInput o : this.input)
      unmatched.add(o); 
    double outputCharge = 0.0D;
    for (int i = 0; i < offerSize; i++) {
      ItemStack offer = inventorycrafting.getStackInSlot(i);
      if (!StackUtil.isEmpty(offer)) {
        int j = 0;
        while (true) {
          if (j < unmatched.size()) {
            if (((IRecipeInput)unmatched.get(j)).matches(offer)) {
              outputCharge += ElectricItem.manager.getCharge(StackUtil.copyWithSize(offer, 1));
              unmatched.remove(j);
              break;
            } 
            j++;
            continue;
          } 
          return StackUtil.emptyStack;
        } 
      } 
    } 
    if (!unmatched.isEmpty())
      return StackUtil.emptyStack; 
    ItemStack ret = this.output.copy();
    ElectricItem.manager.charge(ret, outputCharge, 2147483647, true, false);
    return ret;
  }
  
  public ItemStack getRecipeOutput() {
    return this.output;
  }
  
  public boolean canShow() {
    return AdvRecipe.canShow((Object[])this.input, this.output, this.hidden);
  }
  
  public NonNullList<ItemStack> getRemainingItems(InventoryCrafting inv) {
    return this.consuming ? NonNullList.withSize(inv.getSizeInventory(), StackUtil.emptyStack) : ForgeHooks.defaultRecipeGetRemainingItems(inv);
  }
  
  public IRecipe setRegistryName(ResourceLocation name) {
    this.name = name;
    return this;
  }
  
  public ResourceLocation getRegistryName() {
    return this.name;
  }
  
  public Class<IRecipe> getRegistryType() {
    return IRecipe.class;
  }
  
  public boolean canFit(int x, int y) {
    return (x * y >= this.input.length);
  }
  
  public NonNullList<Ingredient> getIngredients() {
    NonNullList<Ingredient> list = NonNullList.create();
    if (!this.hidden)
      for (IRecipeInput input : this.input)
        list.add(input.getIngredient());  
    return list;
  }
  
  public boolean isDynamic() {
    return this.hidden;
  }
}
