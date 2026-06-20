package ic2.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.core.gui.dynamic.GuiParser;
import ic2.core.util.Tuple;
import ic2.integration.jeirei.SlotPosition;

import java.util.ArrayList;
import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import org.jetbrains.annotations.NotNull;

public class CannerBottleCategory extends IORecipeCategory<CannerBottleRecipeWrapper>
{
	private final int xOffset;
	private final int yOffset;
	private final List<Tuple.T2<IDrawable, SlotPosition>> elements = new ArrayList<>();
	private final List<SlotPosition> inputSlots = new ArrayList<>();
	private final List<SlotPosition> outputSlots = new ArrayList<>();
	private final IDrawable background;
	private final RecipeType<CannerBottleRecipeWrapper> recipeType;

	public CannerBottleCategory(Ic2TileEntityBlock teBlock, RecipeType<CannerBottleRecipeWrapper> recipeType, IGuiHelper guiHelper)
	{
		super(teBlock);
		this.recipeType = recipeType;
		DynamicCategory.parseWidgets(teBlock, guiHelper,
			GuiParser.parse(BuiltInRegistries.BLOCK.getKey(teBlock), teBlock.getDummyTe().getClass()),
			this.elements, this.inputSlots, this.outputSlots);

		int minX = 1000, minY = 1000, maxX = -1000, maxY = -1000;
		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			minX = Math.min(minX, element.b.x());
			minY = Math.min(minY, element.b.y());
			maxX = Math.max(maxX, element.b.x() + element.a.getWidth());
			maxY = Math.max(maxY, element.b.y() + element.a.getHeight());
		}
		this.xOffset = -minX;
		this.yOffset = -minY;
		this.background = guiHelper.createBlankDrawable(maxX - minX, maxY - minY);
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CannerBottleRecipeWrapper recipe, @NotNull IFocusGroup focuses)
	{
		this.addRecipeSlots(builder, recipe, focuses, this.xOffset, this.yOffset);
	}

	@Override
	public void draw(CannerBottleRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		for (Tuple.T2<IDrawable, SlotPosition> element : this.elements)
		{
			element.a.draw(guiGraphics, element.b.x() + this.xOffset, element.b.y() + this.yOffset);
		}
	}

	@Override
	public @NotNull RecipeType<CannerBottleRecipeWrapper> getRecipeType()
	{
		return this.recipeType;
	}

	public int getWidth()
	{
		return this.background.getWidth();
	}

	public int getHeight()
	{
		return this.background.getHeight();
	}

	@Override
	protected List<SlotPosition> getInputSlotPos()
	{
		return this.inputSlots;
	}

	@Override
	protected List<SlotPosition> getOutputSlotPos()
	{
		return this.outputSlots;
	}
}
