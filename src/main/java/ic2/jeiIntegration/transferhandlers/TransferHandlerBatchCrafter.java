package ic2.jeiIntegration.transferhandlers;

import ic2.core.IC2;
import ic2.core.block.machine.container.ContainerBatchCrafter;
import ic2.core.util.StackUtil;

import java.util.Map;

import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IGuiItemStackGroup;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class TransferHandlerBatchCrafter implements IRecipeTransferHandler<ContainerBatchCrafter>
{
	public Class<ContainerBatchCrafter> getContainerClass()
	{
		return ContainerBatchCrafter.class;
	}

	public IRecipeTransferError transferRecipe(
		ContainerBatchCrafter container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer
	)
	{
		if (!doTransfer)
		{
			return null;
		}

		IGuiItemStackGroup stacks = recipeLayout.getItemStacks();
		Map<Integer, ? extends IGuiIngredient<ItemStack>> slotToStackMap = stacks.getGuiIngredients();

		for (int i = 0; i < 9; i++)
		{
			IGuiIngredient<ItemStack> currentIngredient = (IGuiIngredient<ItemStack>) slotToStackMap.get(i + 1);
			ItemStack set;
			if (currentIngredient != null)
			{
				set = (ItemStack) currentIngredient.getDisplayedIngredient();
			} else
			{
				set = StackUtil.emptyStack;
			}

			container.base.craftingGrid[i] = set;
		}

		IC2.network.get(false).updateTileEntityField(container.base, "craftingGrid");
		IC2.network.get(false).initiateClientTileEntityEvent(container.base, 0);
		return null;
	}
}
