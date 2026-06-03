package ic2.jeiIntegration.recipe.misc;

import ic2.api.recipe.Recipes;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ScrapboxRecipeWrapper extends BlankRecipeWrapper {
  private final Map.Entry<ItemStack, Float> entry;
  
  public ScrapboxRecipeWrapper(Map.Entry<ItemStack, Float> entry) {
    this.entry = entry;
  }
  
  public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
    String text;
    float value = ((Float)this.entry.getValue()).floatValue();
    if (value < 0.001D) {
      text = "< 0.01";
    } else {
      text = "  " + String.format("%.2f", new Object[] { Float.valueOf(value * 100.0F) });
    } 
    minecraft.field_71466_p.func_78276_b(text + "%", 86, 9, 4210752);
  }
  
  public static List<ScrapboxRecipeWrapper> createRecipes() {
    List<ScrapboxRecipeWrapper> recipes = new ArrayList<>();
    for (Map.Entry<ItemStack, Float> e : (Iterable<Map.Entry<ItemStack, Float>>)Recipes.scrapboxDrops.getDrops().entrySet())
      recipes.add(new ScrapboxRecipeWrapper(e)); 
    return recipes;
  }
  
  public void getIngredients(IIngredients ingredients) {
    ingredients.setInput(ItemStack.class, ItemName.crafting.getItemStack((Enum)CraftingItemType.scrap_box));
    ingredients.setOutput(ItemStack.class, this.entry.getKey());
  }
}
