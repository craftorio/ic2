package ic2.integration.jei.recipe.machine;

import net.minecraft.client.gui.GuiGraphics;
import ic2.core.IC2;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
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

import java.util.List;

public class CannerBottleLiquidCategory implements IRecipeCategory<CannerBottleLiquidRecipeWrapper>
{
	private static final ResourceLocation GUI_TEXTURE = IC2.getIdentifier("textures/gui/guicanner.png");
	private final RecipeType<CannerBottleLiquidRecipeWrapper> recipeType;

	public CannerBottleLiquidCategory(RecipeType<CannerBottleLiquidRecipeWrapper> recipeType, IGuiHelper guiHelper)
	{
		this.recipeType = recipeType;
	}

	@Override
	public @NotNull RecipeType<CannerBottleLiquidRecipeWrapper> getRecipeType()
	{
		return this.recipeType;
	}

	@Override
	public @NotNull Component getTitle()
	{
		return Component.translatable("ic2.Canner.gui.switch.BottleLiquid");
	}

	@Override
	public int getWidth()
	{
		return 132;
	}

	@Override
	public int getHeight()
	{
		return 54;
	}

	@Override
	public IDrawable getIcon()
	{
		return null;
	}

	@Override
	public void setRecipe(@NotNull IRecipeLayoutBuilder builder, @NotNull CannerBottleLiquidRecipeWrapper recipe, @NotNull IFocusGroup focuses)
	{
		builder.addSlot(RecipeIngredientRole.INPUT, 12, 11)
			.addFluidStack(recipe.getFluidInput().getFluid(), recipe.getFluidInput().getAmount())
			.setFluidRenderer(8000, false, 18, 18);

		IRecipeSlotBuilder containerSlot = builder.addSlot(RecipeIngredientRole.INPUT, 48, 11);
		List<ItemStack> emptyContainers = recipe.getEmptyContainers();
		if (!emptyContainers.isEmpty())
		{
			containerSlot.addItemStacks(emptyContainers);
		}

		builder.addSlot(RecipeIngredientRole.OUTPUT, 105, 11)
			.addItemStack(recipe.getFilledContainer());
	}

	@Override
	public void draw(@NotNull CannerBottleLiquidRecipeWrapper recipe, @NotNull IRecipeSlotsView recipeSlotsView, @NotNull GuiGraphics guiGraphics, double mouseX, double mouseY)
	{
		guiGraphics.blit(GUI_TEXTURE, 74, 13, 233, 0, 23, 14);
		guiGraphics.blit(GUI_TEXTURE, 36, 8, 3, 4, 18, 23);
	}
}
