package ic2.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;
import ic2.core.ref.Ic2Blocks;
import ic2.integration.jeirei.SlotPosition;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class CannerEnrichCategory implements IRecipeCategory<CannerEnrichRecipeWrapper>
{
	private static final ResourceLocation ARROW_TEXTURE = IC2.getIdentifier("textures/gui/overlay/canner_arrow");
	private final RecipeType<CannerEnrichRecipeWrapper> recipeType;
	private final IDrawable background;
	private final IDrawableStatic arrow;

	public CannerEnrichCategory(RecipeType<CannerEnrichRecipeWrapper> recipeType, IGuiHelper guiHelper)
	{
		this.recipeType = recipeType;
		this.background = guiHelper.createBlankDrawable(132, 54);
		this.arrow = guiHelper.createDrawable(ARROW_TEXTURE, 0, 0, 12, 18);
	}

	@Override
	public @NotNull RecipeType<CannerEnrichRecipeWrapper> getRecipeType()
	{
		return this.recipeType;
	}

	@Override
	public Component getTitle()
	{
		return Component.translatable("ic2.Canner.gui.switch.EnrichLiquid");
	}

	@Override
	public IDrawable getBackground()
	{
		return this.background;
	}

	@Override
	public IDrawable getIcon()
	{
		return null;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CannerEnrichRecipeWrapper recipe, @NotNull IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 12, 11)
			.addFluidStack(recipe.getFluidInput().getFluid(), recipe.getFluidInput().getAmount())
			.setFluidRenderer(8000, false, 18, 18);

		IRecipeSlotBuilder additiveSlot = builder.addSlot(RecipeIngredientRole.INPUT, 48, 11);
		List<ItemStack> additiveInputs = recipe.getAdditiveInputs();
		if (!additiveInputs.isEmpty())
		{
			additiveSlot.addItemStacks(additiveInputs);
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 11)
			.addFluidStack(recipe.getFluidOutput().getFluid(), recipe.getFluidOutput().getAmount())
			.setFluidRenderer(8000, false, 18, 18);
	}

	@Override
	public void draw(CannerEnrichRecipeWrapper recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		this.arrow.draw(guiGraphics, 76, 12);
	}
}
