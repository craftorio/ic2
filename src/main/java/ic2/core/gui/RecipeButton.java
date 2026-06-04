// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.gui;

import ic2.core.init.Localization;
import java.util.List;
import ic2.core.GuiIC2;
import com.google.common.base.Function;

public class RecipeButton extends Button<RecipeButton>
{
    public static Function<String[], IClickHandler> jeiRecipeListOpener;
    
    public static boolean canUse() {
        return RecipeButton.jeiRecipeListOpener != null;
    }
    
    public RecipeButton(final GuiElement<?> wrapping, final String[] categories) {
        this(wrapping.gui, wrapping.x, wrapping.y, wrapping.width, wrapping.height, categories);
    }
    
    public RecipeButton(final GuiIC2<?> gui, final int x, final int y, final int width, final int height, final String[] categories) {
        super(gui, x, y, width, height, (IClickHandler)RecipeButton.jeiRecipeListOpener.apply((Object)categories));
    }
    
    @Override
    protected List<String> getToolTip() {
        final List<String> ret = super.getToolTip();
        ret.add(Localization.translate("ic2.jei.recipes"));
        return ret;
    }
    
    @SkippedMethod
    @Override
    public void drawBackground(final int mouseX, final int mouseY) {
    }
}
