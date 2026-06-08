package ic2.integration.jei.recipe.machine;

import ic2.core.block.tileentity.Ic2TileEntityBlock;
import ic2.integration.jeirei.SlotPosition;

import java.util.List;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public abstract class IORecipeCategory implements IRecipeCategory<IORecipeWrapper>
{
	protected final Ic2TileEntityBlock block;

	public IORecipeCategory(Ic2TileEntityBlock block)
	{
		this.block = block;
	}

	public Component getTitle()
	{
		return this.getBlockStack().getHoverName();
	}

	protected abstract List<SlotPosition> getInputSlotPos();

	protected abstract List<SlotPosition> getOutputSlotPos();

	protected void addRecipeSlots(IRecipeLayoutBuilder builder, IORecipeWrapper recipe, IFocusGroup focuses, int xOffset, int yOffset)
	{
		List<SlotPosition> inputSlots = this.getInputSlotPos();
		List<List<ItemStack>> inputStacks = recipe.getInputs();

		int idx;
		for (idx = 0; idx < inputSlots.size(); idx++)
		{
			SlotPosition pos = inputSlots.get(idx);
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.INPUT, pos.x() + xOffset, pos.y() + yOffset);
			if (idx < inputStacks.size())
			{
				slot.addItemStacks(inputStacks.get(idx));
			}
		}

		List<SlotPosition> outputSlots = this.getOutputSlotPos();
		List<List<ItemStack>> outputStacks = List.of(recipe.getOutputs());

		for (int i = 0; i < outputSlots.size(); idx++)
		{
			SlotPosition pos = outputSlots.get(i);
			IRecipeSlotBuilder slot = builder.addSlot(RecipeIngredientRole.OUTPUT, pos.x() + xOffset, pos.y() + yOffset);
			if (i < outputStacks.size())
			{
				slot.addItemStacks(outputStacks.get(i));
			}

			i++;
		}
	}

	public ItemStack getBlockStack()
	{
		return new ItemStack(this.block.asItem());
	}

	public IDrawable getIcon()
	{
		return null;
	}
}
