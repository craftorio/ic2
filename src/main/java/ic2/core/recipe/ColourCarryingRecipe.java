package ic2.core.recipe;

import ic2.core.init.MainConfig;
import ic2.core.init.Rezepte;
import ic2.core.util.StackUtil;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;

public class ColourCarryingRecipe extends AdvRecipe {
  public static void addAndRegister(ItemStack result, Object... args) {
    try {
      Rezepte.registerRecipe((IRecipe)new ColourCarryingRecipe(result, args));
    } catch (RuntimeException e) {
      if (!MainConfig.ignoreInvalidRecipes)
        throw e; 
    } 
  }
  
  public ColourCarryingRecipe(ItemStack result, Object... args) {
    super(result, args);
  }
  
  public ItemStack getCraftingResult(InventoryCrafting craftingInv) {
    ItemStack initialResult = super.getCraftingResult(craftingInv);
    if (!StackUtil.isEmpty(initialResult) && initialResult.getItem() instanceof ItemArmor) {
      int colour = -1;
      for (int slot = 0; slot < craftingInv.getSizeInventory(); slot++) {
        ItemStack offer = craftingInv.getStackInSlot(slot);
        if (!StackUtil.isEmpty(initialResult) && offer.getItem() instanceof ItemArmor) {
          colour = ((ItemArmor)offer.getItem()).getColor(offer);
          break;
        } 
      } 
      if (colour != -1)
        ((ItemArmor)initialResult.getItem()).setColor(initialResult, colour); 
    } 
    return initialResult;
  }
}
