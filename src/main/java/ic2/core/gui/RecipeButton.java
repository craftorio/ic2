package ic2.core.gui;

import com.google.common.base.Function;
import ic2.core.GuiIC2;
import ic2.core.init.Localization;
import java.util.List;

public class RecipeButton extends Button<RecipeButton> {
   public static Function<String[], IClickHandler> jeiRecipeListOpener;

   public static boolean canUse() {
      return jeiRecipeListOpener != null;
   }

   public RecipeButton(GuiElement<?> wrapping, String[] categories) {
      this(wrapping.gui, wrapping.x, wrapping.y, wrapping.width, wrapping.height, categories);
   }

   public RecipeButton(GuiIC2<?> gui, int x, int y, int width, int height, String[] categories) {
      super(gui, x, y, width, height, (IClickHandler)jeiRecipeListOpener.apply(categories));
   }

   @Override
   protected List<String> getToolTip() {
      List<String> ret = super.getToolTip();
      ret.add(Localization.translate("ic2.jei.recipes"));
      return ret;
   }

   @GuiElement.SkippedMethod
   @Override
   public void drawBackground(int mouseX, int mouseY) {
   }
}
