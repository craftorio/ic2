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
      result = result.func_77946_l();
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
  
  public boolean func_77569_a(InventoryCrafting inventorycrafting, World world) {
    return (func_77572_b(inventorycrafting) != StackUtil.emptyStack);
  }
  
  public ItemStack func_77572_b(InventoryCrafting inventorycrafting) {
    int offerSize = inventorycrafting.func_70302_i_();
    if (offerSize < this.input.length)
      return StackUtil.emptyStack; 
    List<IRecipeInput> unmatched = new Vector<>();
    for (IRecipeInput o : this.input)
      unmatched.add(o); 
    double outputCharge = 0.0D;
    for (int i = 0; i < offerSize; i++) {
      ItemStack offer = inventorycrafting.func_70301_a(i);
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
    ItemStack ret = this.output.func_77946_l();
    ElectricItem.manager.charge(ret, outputCharge, 2147483647, true, false);
    return ret;
  }
  
  public ItemStack func_77571_b() {
    return this.output;
  }
  
  public boolean canShow() {
    return AdvRecipe.canShow((Object[])this.input, this.output, this.hidden);
  }
  
  public NonNullList<ItemStack> func_179532_b(InventoryCrafting inv) {
    return this.consuming ? NonNullList.func_191197_a(inv.func_70302_i_(), StackUtil.emptyStack) : ForgeHooks.defaultRecipeGetRemainingItems(inv);
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
  
  public boolean func_194133_a(int x, int y) {
    return (x * y >= this.input.length);
  }
  
  public NonNullList<Ingredient> func_192400_c() {
    NonNullList<Ingredient> list = NonNullList.func_191196_a();
    if (!this.hidden)
      for (IRecipeInput input : this.input)
        list.add(input.getIngredient());  
    return list;
  }
  
  public boolean func_192399_d() {
    return this.hidden;
  }
}
