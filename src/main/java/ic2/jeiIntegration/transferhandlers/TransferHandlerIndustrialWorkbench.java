package ic2.jeiIntegration.transferhandlers;

import ic2.core.block.machine.container.ContainerIndustrialWorkbench;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import mezz.jei.JustEnoughItems;
import mezz.jei.api.gui.IGuiIngredient;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.startup.StackHelper;
import mezz.jei.startup.StackHelper.MatchingItemsResult;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TransferHandlerIndustrialWorkbench implements IRecipeTransferHandler<ContainerIndustrialWorkbench>
{
	private final IRecipeTransferHandler<ContainerIndustrialWorkbench> crafting;
	private final IRecipeTransferHandler<ContainerIndustrialWorkbench> others;

	public TransferHandlerIndustrialWorkbench(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper)
	{
		TransferHandlerIndustrialWorkbench.TransferInfo info = new TransferHandlerIndustrialWorkbench.TransferInfo();
		this.crafting = new BasicRecipeTransferHandler(stackHelper, handlerHelper, info);
		this.others = new TransferHandlerIndustrialWorkbench.AdjustedRecipeTransferHandler(
			stackHelper, handlerHelper, new TransferHandlerIndustrialWorkbench.TransferInfo()
		);
	}

	public Class<ContainerIndustrialWorkbench> getContainerClass()
	{
		return ContainerIndustrialWorkbench.class;
	}

	@Nullable
	public IRecipeTransferError transferRecipe(
		ContainerIndustrialWorkbench container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer
	)
	{
		IRecipeTransferError error = this.others.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
		return error == null ? error : this.crafting.transferRecipe(container, recipeLayout, player, maxTransfer, doTransfer);
	}

	private static class AdjustedRecipeTransferHandler implements IRecipeTransferHandler<ContainerIndustrialWorkbench>
	{
		private final StackHelper stackHelper;
		private final IRecipeTransferHandlerHelper handlerHelper;
		private final IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper;

		public AdjustedRecipeTransferHandler(
			StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper, IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper
		)
		{
			this.stackHelper = stackHelper;
			this.handlerHelper = handlerHelper;
			this.transferHelper = transferHelper;
		}

		public Class<ContainerIndustrialWorkbench> getContainerClass()
		{
			return this.transferHelper.getContainerClass();
		}

		@Nullable
		public IRecipeTransferError transferRecipe(
			ContainerIndustrialWorkbench container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer
		)
		{
			List<IGuiIngredient<ItemStack>> ingredients = new ArrayList<>();
			recipeLayout.getItemStacks()
				.getGuiIngredients()
				.values()
				.stream()
				.filter(IGuiIngredient::isInput)
				.filter(i -> !i.getAllIngredients().isEmpty())
				.forEach(ingredients::add);
			if (ingredients.size() != 2)
			{
				return this.handlerHelper.createInternalError();
			}

			Slot toolLeft = container.getSlot(container.indexOutputHammer - 2);
			Slot toolRight = container.getSlot(container.indexOutputCutter - 2);
			Slot itemLeft = container.getSlot(container.indexOutputHammer - 1);
			Slot itemRight = container.getSlot(container.indexOutputCutter - 1);
			Slot[][] craftingSlots = new Slot[][] { { toolLeft, itemLeft }, { toolRight, itemRight } };
			int toolIdx = -1;
			int craftingIdx = -1;

			for (int i = 0; i < ingredients.size(); i++)
			{
				ItemStack stack = (ItemStack) ingredients.get(i).getDisplayedIngredient();
				if (toolLeft.isItemValid(stack))
				{
					toolIdx = i;
					craftingIdx = 0;
					break;
				}

				if (toolRight.isItemValid(stack))
				{
					toolIdx = i;
					craftingIdx = 1;
					break;
				}
			}

			if (toolIdx == -1)
			{
				return this.handlerHelper.createInternalError();
			}

			Map<Integer, IGuiIngredient<ItemStack>> adjustedIngredients = new HashMap<>();
			adjustedIngredients.put(0, ingredients.get(toolIdx));
			adjustedIngredients.put(1, ingredients.get(1 - toolIdx));
			Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
			int filledCraftSlotCount = 0;
			int emptySlotCount = 0;

			for (Slot slot : craftingSlots[craftingIdx])
			{
				ItemStack stack = slot.getStack();
				if (!stack.isEmpty())
				{
					if (!slot.canTakeStack(player))
					{
						Log.get()
							.error(
								"Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}",
								this.transferHelper.getClass(),
								container.getClass(),
								slot.slotNumber
							);
						return this.handlerHelper.createInternalError();
					}

					filledCraftSlotCount++;
					availableItemStacks.put(slot.slotNumber, stack.copy());
				}
			}

			List<Slot> inventorySlots = this.transferHelper.getInventorySlots(container);

			for (Slot slot : inventorySlots)
			{
				ItemStack stack = slot.getStack();
				if (!stack.isEmpty())
				{
					availableItemStacks.put(slot.slotNumber, stack.copy());
				} else
				{
					emptySlotCount++;
				}
			}

			if (filledCraftSlotCount - ingredients.size() > emptySlotCount)
			{
				String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
				return this.handlerHelper.createUserErrorWithTooltip(message);
			}

			MatchingItemsResult matchingItemsResult = this.stackHelper.getMatchingItems(availableItemStacks, adjustedIngredients);
			if (matchingItemsResult.missingItems.size() > 0)
			{
				String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
				return this.handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
			}

			List<Integer> inventorySlotIndexes = new ArrayList<>();
			inventorySlots.stream().map(s -> s.slotNumber).forEach(inventorySlotIndexes::add);
			if (doTransfer)
			{
				List<Integer> craftingSlotIndexes = Arrays.asList(craftingSlots[craftingIdx][0].slotNumber, craftingSlots[craftingIdx][1].slotNumber);
				PacketRecipeTransfer packet = new PacketRecipeTransfer(
					matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer, false
				);
				JustEnoughItems.getProxy().sendPacketToServer(packet);
			}

			return null;
		}
	}

	private static class TransferInfo implements IRecipeTransferInfo<ContainerIndustrialWorkbench>
	{
		private TransferInfo()
		{
		}

		public Class<ContainerIndustrialWorkbench> getContainerClass()
		{
			return null;
		}

		public String getRecipeCategoryUid()
		{
			return null;
		}

		public boolean canHandle(ContainerIndustrialWorkbench container)
		{
			return true;
		}

		public List<Slot> getRecipeSlots(ContainerIndustrialWorkbench container)
		{
			List<Slot> recipeSlots = new ArrayList<>();

			for (int i = container.indexGridStart; i < container.indexGridEnd; i++)
			{
				recipeSlots.add(container.getSlot(i));
			}

			return recipeSlots;
		}

		public List<Slot> getInventorySlots(ContainerIndustrialWorkbench container)
		{
			List<Slot> inventorySlots = new ArrayList<>();

			for (int i = container.indexBufferStart; i < container.indexBufferEnd; i++)
			{
				inventorySlots.add(container.getSlot(i));
			}

			for (int i = 0; i < 36; i++)
			{
				inventorySlots.add(container.getSlot(i));
			}

			return inventorySlots;
		}
	}
}
