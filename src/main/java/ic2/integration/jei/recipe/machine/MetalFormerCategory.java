package ic2.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.ref.Ic2Blocks;
import ic2.core.ref.Ic2Items;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.ItemStack;

public class MetalFormerCategory extends DynamicCategory
{
	private final int mode;
	private static final ItemStack[] ICONS = new ItemStack[] {
		new ItemStack(Ic2Items.COPPER_CABLE), new ItemStack(Ic2Items.FORGE_HAMMER), new ItemStack(Ic2Items.CUTTER)
	};

	public MetalFormerCategory(RecipeType<IORecipeWrapper> recipeType, int mode, IGuiHelper guiHelper)
	{
		super(Ic2Blocks.METAL_FORMER, recipeType, guiHelper);
		this.mode = mode;
	}

	@Override
	public void draw(IORecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		super.draw(recipe, recipeSlotsView, guiGraphics, mouseX, mouseY);
		guiGraphics.renderItem(ICONS[this.mode], 65 + this.xOffset, 53 + this.yOffset);
	}
}
