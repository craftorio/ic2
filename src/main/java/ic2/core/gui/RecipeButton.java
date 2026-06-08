package ic2.core.gui;

import com.google.common.base.Function;
import com.mojang.blaze3d.vertex.PoseStack;
import ic2.core.Ic2Gui;

import java.util.List;

import net.minecraft.network.chat.Component;

public class RecipeButton extends Button<RecipeButton>
{
	public static Function<String[], IClickHandler> jeiRecipeListOpener;

	public static boolean canUse()
	{
		return jeiRecipeListOpener != null;
	}

	public RecipeButton(GuiElement<?> wrapping, String[] categories)
	{
		this(wrapping.gui, wrapping.x, wrapping.y, wrapping.width, wrapping.height, categories);
	}

	public RecipeButton(Ic2Gui<?> gui, int x, int y, int width, int height, String[] categories)
	{
		super(gui, x, y, width, height, (IClickHandler) jeiRecipeListOpener.apply(categories));
	}

	@Override
	protected List<Component> getToolTip()
	{
		List<Component> ret = super.getToolTip();
		ret.add(Component.translatable("ic2.jei.recipes"));
		return ret;
	}

	@GuiElement.SkippedMethod
	@Override
	public void drawBackground(PoseStack matrices, int mouseX, int mouseY)
	{
	}
}
