package ic2.jeiIntegration.recipe.crafting;

import ic2.core.item.armor.jetpack.JetpackAttachmentRecipe;
import ic2.core.item.armor.jetpack.JetpackHandler;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.ItemComparableItemStack;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLiving;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class JetpackRecipeWrapper extends BlankRecipeWrapper {
  private final ItemStack in;
  
  private final ItemStack out;
  
  private static List<JetpackRecipeWrapper> jetpackRecipes;
  
  private JetpackRecipeWrapper(ItemStack in) {
    this.in = in;
    ItemStack out = in.copy();
    JetpackHandler.setJetpackAttached(out, true);
    this.out = out;
  }
  
  public static List<JetpackRecipeWrapper> generateJetpackRecipes() {
    if (jetpackRecipes != null)
      return jetpackRecipes; 
    NonNullList<ItemStack> stacks = NonNullList.func_191196_a();
    Set<ItemComparableItemStack> added = new HashSet<>();
    jetpackRecipes = new ArrayList<>(100);
    for (Item item : ForgeRegistries.ITEMS) {
      if (JetpackAttachmentRecipe.blacklistedItems.contains(item))
        continue; 
      stacks.clear();
      added.clear();
      item.func_150895_a(CreativeTabs.field_78027_g, stacks);
      for (ItemStack stack : stacks) {
        if (EntityLiving.func_184640_d(stack) == EntityEquipmentSlot.CHEST) {
          ItemComparableItemStack comparable = new ItemComparableItemStack(stack, false);
          if (!added.contains(comparable)) {
            jetpackRecipes.add(new JetpackRecipeWrapper(stack));
            added.add(comparable);
          } 
        } 
      } 
    } 
    return jetpackRecipes;
  }
  
  public void getIngredients(IIngredients ingredients) {
    ingredients.setInputs(ItemStack.class, Arrays.asList(new ItemStack[] { ItemName.jetpack_electric.getItemStack(), ItemName.crafting.getItemStack((Enum)CraftingItemType.jetpack_attachment_plate), this.in }));
    ingredients.setOutput(ItemStack.class, this.out);
  }
}
