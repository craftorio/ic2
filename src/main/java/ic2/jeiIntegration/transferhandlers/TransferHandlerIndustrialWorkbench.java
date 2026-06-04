// 
// Decompiled by Procyon v0.6.0
// 

package ic2.jeiIntegration.transferhandlers;

import java.util.Iterator;
import mezz.jei.network.packets.PacketJei;
import mezz.jei.JustEnoughItems;
import mezz.jei.network.packets.PacketRecipeTransfer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import mezz.jei.util.Translator;
import mezz.jei.util.Log;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.item.ItemStack;
import mezz.jei.api.gui.IGuiIngredient;
import java.util.ArrayList;
import net.minecraft.inventory.Slot;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.inventory.Container;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import net.minecraft.entity.player.EntityPlayer;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.recipe.transfer.IRecipeTransferInfo;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.startup.StackHelper;
import ic2.core.block.machine.container.ContainerIndustrialWorkbench;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;

public class TransferHandlerIndustrialWorkbench implements IRecipeTransferHandler<ContainerIndustrialWorkbench>
{
    private final IRecipeTransferHandler<ContainerIndustrialWorkbench> crafting;
    private final IRecipeTransferHandler<ContainerIndustrialWorkbench> others;
    
    public TransferHandlerIndustrialWorkbench(final StackHelper stackHelper, final IRecipeTransferHandlerHelper handlerHelper) {
        final TransferInfo info = new TransferInfo();
        this.crafting = (IRecipeTransferHandler<ContainerIndustrialWorkbench>)new BasicRecipeTransferHandler(stackHelper, handlerHelper, (IRecipeTransferInfo)info);
        this.others = (IRecipeTransferHandler<ContainerIndustrialWorkbench>)new AdjustedRecipeTransferHandler(stackHelper, handlerHelper, (IRecipeTransferInfo<ContainerIndustrialWorkbench>)new TransferInfo());
    }
    
    public Class<ContainerIndustrialWorkbench> getContainerClass() {
        return ContainerIndustrialWorkbench.class;
    }
    
    @Nullable
    public IRecipeTransferError transferRecipe(final ContainerIndustrialWorkbench container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer) {
        final IRecipeTransferError error = this.others.transferRecipe((Container)container, recipeLayout, player, maxTransfer, doTransfer);
        if (error == null) {
            return error;
        }
        return this.crafting.transferRecipe((Container)container, recipeLayout, player, maxTransfer, doTransfer);
    }
    
    private static class TransferInfo implements IRecipeTransferInfo<ContainerIndustrialWorkbench>
    {
        public Class<ContainerIndustrialWorkbench> getContainerClass() {
            return null;
        }
        
        public String getRecipeCategoryUid() {
            return null;
        }
        
        public boolean canHandle(final ContainerIndustrialWorkbench container) {
            return true;
        }
        
        public List<Slot> getRecipeSlots(final ContainerIndustrialWorkbench container) {
            final List<Slot> recipeSlots = new ArrayList<Slot>();
            for (int i = container.indexGridStart; i < container.indexGridEnd; ++i) {
                recipeSlots.add(container.getSlot(i));
            }
            return recipeSlots;
        }
        
        public List<Slot> getInventorySlots(final ContainerIndustrialWorkbench container) {
            final List<Slot> inventorySlots = new ArrayList<Slot>();
            for (int i = container.indexBufferStart; i < container.indexBufferEnd; ++i) {
                inventorySlots.add(container.getSlot(i));
            }
            for (int i = 0; i < 36; ++i) {
                inventorySlots.add(container.getSlot(i));
            }
            return inventorySlots;
        }
    }
    
    private static class AdjustedRecipeTransferHandler implements IRecipeTransferHandler<ContainerIndustrialWorkbench>
    {
        private final StackHelper stackHelper;
        private final IRecipeTransferHandlerHelper handlerHelper;
        private final IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper;
        
        public AdjustedRecipeTransferHandler(final StackHelper stackHelper, final IRecipeTransferHandlerHelper handlerHelper, final IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper) {
            this.stackHelper = stackHelper;
            this.handlerHelper = handlerHelper;
            this.transferHelper = transferHelper;
        }
        
        public Class<ContainerIndustrialWorkbench> getContainerClass() {
            return this.transferHelper.getContainerClass();
        }
        
        @Nullable
        public IRecipeTransferError transferRecipe(final ContainerIndustrialWorkbench container, final IRecipeLayout recipeLayout, final EntityPlayer player, final boolean maxTransfer, final boolean doTransfer) {
            final List<IGuiIngredient<ItemStack>> ingredients = new ArrayList<IGuiIngredient<ItemStack>>();
            int i = 0;
            recipeLayout.getItemStacks().getGuiIngredients().values().stream().filter(IGuiIngredient::isInput).filter(i -> !i.getAllIngredients().isEmpty()).forEach(ingredients::add);
            if (ingredients.size() != 2) {
                return this.handlerHelper.createInternalError();
            }
            final Slot toolLeft = container.getSlot(container.indexOutputHammer - 2);
            final Slot toolRight = container.getSlot(container.indexOutputCutter - 2);
            final Slot itemLeft = container.getSlot(container.indexOutputHammer - 1);
            final Slot itemRight = container.getSlot(container.indexOutputCutter - 1);
            final Slot[][] craftingSlots = { { toolLeft, itemLeft }, { toolRight, itemRight } };
            int toolIdx = -1;
            int craftingIdx = -1;
            for (i = 0; i < ingredients.size(); ++i) {
                final ItemStack stack = (ItemStack)ingredients.get(i).getDisplayedIngredient();
                if (toolLeft.isItemValid(stack)) {
                    toolIdx = i;
                    craftingIdx = 0;
                    break;
                }
                if (toolRight.isItemValid(stack)) {
                    toolIdx = i;
                    craftingIdx = 1;
                    break;
                }
            }
            if (toolIdx == -1) {
                return this.handlerHelper.createInternalError();
            }
            final Map<Integer, IGuiIngredient<ItemStack>> adjustedIngredients = new HashMap<Integer, IGuiIngredient<ItemStack>>();
            adjustedIngredients.put(0, ingredients.get(toolIdx));
            adjustedIngredients.put(1, ingredients.get(1 - toolIdx));
            final Map<Integer, ItemStack> availableItemStacks = new HashMap<Integer, ItemStack>();
            int filledCraftSlotCount = 0;
            int emptySlotCount = 0;
            for (final Slot slot : craftingSlots[craftingIdx]) {
                final ItemStack stack2 = slot.getStack();
                if (!stack2.isEmpty()) {
                    if (!slot.canTakeStack(player)) {
                        Log.get().error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", (Object)this.transferHelper.getClass(), (Object)container.getClass(), (Object)slot.slotNumber);
                        return this.handlerHelper.createInternalError();
                    }
                    ++filledCraftSlotCount;
                    availableItemStacks.put(slot.slotNumber, stack2.copy());
                }
            }
            final List<Slot> inventorySlots = this.transferHelper.getInventorySlots((Container)container);
            for (final Slot slot2 : inventorySlots) {
                final ItemStack stack3 = slot2.getStack();
                if (!stack3.isEmpty()) {
                    availableItemStacks.put(slot2.slotNumber, stack3.copy());
                }
                else {
                    ++emptySlotCount;
                }
            }
            if (filledCraftSlotCount - ingredients.size() > emptySlotCount) {
                final String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
                return this.handlerHelper.createUserErrorWithTooltip(message);
            }
            final StackHelper.MatchingItemsResult matchingItemsResult = this.stackHelper.getMatchingItems((Map)availableItemStacks, (Map)adjustedIngredients);
            if (matchingItemsResult.missingItems.size() > 0) {
                final String message2 = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
                return this.handlerHelper.createUserErrorForSlots(message2, (Collection)matchingItemsResult.missingItems);
            }
            final List<Integer> inventorySlotIndexes = new ArrayList<Integer>();
            inventorySlots.stream().map(s -> s.slotNumber).forEach(inventorySlotIndexes::add);
            if (doTransfer) {
                final List<Integer> craftingSlotIndexes = Arrays.asList(craftingSlots[craftingIdx][0].slotNumber, craftingSlots[craftingIdx][1].slotNumber);
                final PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, (List)craftingSlotIndexes, (List)inventorySlotIndexes, maxTransfer, false);
                JustEnoughItems.getProxy().sendPacketToServer((PacketJei)packet);
            }
            return null;
        }
    }
}
