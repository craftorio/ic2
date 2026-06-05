package ic2.jeiIntegration.recipe.misc;

import ic2.api.recipe.Recipes;
import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemStack;

public class ScrapboxRecipeWrapper extends BlankRecipeWrapper {
   private final Entry<ItemStack, Float> entry;

   public ScrapboxRecipeWrapper(Entry<ItemStack, Float> entry) {
      this.entry = entry;
   }

   public void drawInfo(Minecraft minecraft, int recipeWidth, int recipeHeight, int mouseX, int mouseY) {
      float value = this.entry.getValue();
      String text;
      if (value < 0.001) {
         text = "< 0.01";
      } else {
         text = "  " + String.format("%.2f", value * 100.0F);
      }

      minecraft.fontRenderer.drawString(text + "%", 86, 9, 4210752);
   }

   public static List<ScrapboxRecipeWrapper> createRecipes() {
      List<ScrapboxRecipeWrapper> recipes = new ArrayList<>();

      for (Entry<ItemStack, Float> e : Recipes.scrapboxDrops.getDrops().entrySet()) {
         recipes.add(new ScrapboxRecipeWrapper(e));
      }

      return recipes;
   }

   public void getIngredients(IIngredients ingredients) {
      ingredients.setInput(ItemStack.class, ItemName.crafting.getItemStack(CraftingItemType.scrap_box));
      ingredients.setOutput(ItemStack.class, this.entry.getKey());
   }
}
