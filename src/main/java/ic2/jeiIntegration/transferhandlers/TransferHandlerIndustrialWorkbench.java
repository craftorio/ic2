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
import mezz.jei.network.packets.PacketJei;
import mezz.jei.network.packets.PacketRecipeTransfer;
import mezz.jei.startup.StackHelper;
import mezz.jei.transfer.BasicRecipeTransferHandler;
import mezz.jei.util.Log;
import mezz.jei.util.Translator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class TransferHandlerIndustrialWorkbench implements IRecipeTransferHandler<ContainerIndustrialWorkbench> {
  private final IRecipeTransferHandler<ContainerIndustrialWorkbench> crafting;
  
  private final IRecipeTransferHandler<ContainerIndustrialWorkbench> others;
  
  public TransferHandlerIndustrialWorkbench(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper) {
    TransferInfo info = new TransferInfo();
    this.crafting = (IRecipeTransferHandler<ContainerIndustrialWorkbench>)new BasicRecipeTransferHandler(stackHelper, handlerHelper, info);
    this.others = new AdjustedRecipeTransferHandler(stackHelper, handlerHelper, new TransferInfo());
  }
  
  public Class<ContainerIndustrialWorkbench> getContainerClass() {
    return ContainerIndustrialWorkbench.class;
  }
  
  @Nullable
  public IRecipeTransferError transferRecipe(ContainerIndustrialWorkbench container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
    IRecipeTransferError error = this.others.transferRecipe((Container)container, recipeLayout, player, maxTransfer, doTransfer);
    if (error == null)
      return error; 
    return this.crafting.transferRecipe((Container)container, recipeLayout, player, maxTransfer, doTransfer);
  }
  
  private static class TransferInfo implements IRecipeTransferInfo<ContainerIndustrialWorkbench> {
    private TransferInfo() {}
    
    public Class<ContainerIndustrialWorkbench> getContainerClass() {
      return null;
    }
    
    public String getRecipeCategoryUid() {
      return null;
    }
    
    public boolean canHandle(ContainerIndustrialWorkbench container) {
      return true;
    }
    
    public List<Slot> getRecipeSlots(ContainerIndustrialWorkbench container) {
      List<Slot> recipeSlots = new ArrayList<>();
      for (int i = container.indexGridStart; i < container.indexGridEnd; i++)
        recipeSlots.add(container.func_75139_a(i)); 
      return recipeSlots;
    }
    
    public List<Slot> getInventorySlots(ContainerIndustrialWorkbench container) {
      List<Slot> inventorySlots = new ArrayList<>();
      int i;
      for (i = container.indexBufferStart; i < container.indexBufferEnd; i++)
        inventorySlots.add(container.func_75139_a(i)); 
      for (i = 0; i < 36; i++)
        inventorySlots.add(container.func_75139_a(i)); 
      return inventorySlots;
    }
  }
  
  private static class AdjustedRecipeTransferHandler implements IRecipeTransferHandler<ContainerIndustrialWorkbench> {
    private final StackHelper stackHelper;
    
    private final IRecipeTransferHandlerHelper handlerHelper;
    
    private final IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper;
    
    public AdjustedRecipeTransferHandler(StackHelper stackHelper, IRecipeTransferHandlerHelper handlerHelper, IRecipeTransferInfo<ContainerIndustrialWorkbench> transferHelper) {
      this.stackHelper = stackHelper;
      this.handlerHelper = handlerHelper;
      this.transferHelper = transferHelper;
    }
    
    public Class<ContainerIndustrialWorkbench> getContainerClass() {
      return this.transferHelper.getContainerClass();
    }
    
    @Nullable
    public IRecipeTransferError transferRecipe(ContainerIndustrialWorkbench container, IRecipeLayout recipeLayout, EntityPlayer player, boolean maxTransfer, boolean doTransfer) {
      List<IGuiIngredient<ItemStack>> ingredients = new ArrayList<>();
      recipeLayout.getItemStacks().getGuiIngredients().values().stream().filter(IGuiIngredient::isInput).filter(i -> !i.getAllIngredients().isEmpty())
        .forEach(ingredients::add);
      if (ingredients.size() != 2)
        return this.handlerHelper.createInternalError(); 
      Slot toolLeft = container.func_75139_a(container.indexOutputHammer - 2);
      Slot toolRight = container.func_75139_a(container.indexOutputCutter - 2);
      Slot itemLeft = container.func_75139_a(container.indexOutputHammer - 1);
      Slot itemRight = container.func_75139_a(container.indexOutputCutter - 1);
      Slot[][] craftingSlots = { { toolLeft, itemLeft }, { toolRight, itemRight } };
      int toolIdx = -1;
      int craftingIdx = -1;
      for (int i = 0; i < ingredients.size(); i++) {
        ItemStack stack = (ItemStack)((IGuiIngredient)ingredients.get(i)).getDisplayedIngredient();
        if (toolLeft.func_75214_a(stack)) {
          toolIdx = i;
          craftingIdx = 0;
          break;
        } 
        if (toolRight.func_75214_a(stack)) {
          toolIdx = i;
          craftingIdx = 1;
          break;
        } 
      } 
      if (toolIdx == -1)
        return this.handlerHelper.createInternalError(); 
      Map<Integer, IGuiIngredient<ItemStack>> adjustedIngredients = new HashMap<>();
      adjustedIngredients.put(Integer.valueOf(0), ingredients.get(toolIdx));
      adjustedIngredients.put(Integer.valueOf(1), ingredients.get(1 - toolIdx));
      Map<Integer, ItemStack> availableItemStacks = new HashMap<>();
      int filledCraftSlotCount = 0;
      int emptySlotCount = 0;
      for (Slot slot : craftingSlots[craftingIdx]) {
        ItemStack stack = slot.func_75211_c();
        if (!stack.func_190926_b()) {
          if (!slot.func_82869_a(player)) {
            Log.get().error("Recipe Transfer helper {} does not work for container {}. Player can't move item out of Crafting Slot number {}", this.transferHelper.getClass(), container.getClass(), Integer.valueOf(slot.field_75222_d));
            return this.handlerHelper.createInternalError();
          } 
          filledCraftSlotCount++;
          availableItemStacks.put(Integer.valueOf(slot.field_75222_d), stack.copy());
        } 
      } 
      List<Slot> inventorySlots = this.transferHelper.getInventorySlots((Container)container);
      for (Slot slot : inventorySlots) {
        ItemStack stack = slot.func_75211_c();
        if (!stack.func_190926_b()) {
          availableItemStacks.put(Integer.valueOf(slot.field_75222_d), stack.copy());
          continue;
        } 
        emptySlotCount++;
      } 
      if (filledCraftSlotCount - ingredients.size() > emptySlotCount) {
        String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.inventory.full");
        return this.handlerHelper.createUserErrorWithTooltip(message);
      } 
      StackHelper.MatchingItemsResult matchingItemsResult = this.stackHelper.getMatchingItems(availableItemStacks, adjustedIngredients);
      if (matchingItemsResult.missingItems.size() > 0) {
        String message = Translator.translateToLocal("jei.tooltip.error.recipe.transfer.missing");
        return this.handlerHelper.createUserErrorForSlots(message, matchingItemsResult.missingItems);
      } 
      List<Integer> inventorySlotIndexes = new ArrayList<>();
      inventorySlots.stream().map(s -> Integer.valueOf(s.field_75222_d)).forEach(inventorySlotIndexes::add);
      if (doTransfer) {
        List<Integer> craftingSlotIndexes = Arrays.asList(new Integer[] { Integer.valueOf((craftingSlots[craftingIdx][0]).field_75222_d), 
              Integer.valueOf((craftingSlots[craftingIdx][1]).field_75222_d) });
        PacketRecipeTransfer packet = new PacketRecipeTransfer(matchingItemsResult.matchingItems, craftingSlotIndexes, inventorySlotIndexes, maxTransfer, false);
        JustEnoughItems.getProxy().sendPacketToServer((PacketJei)packet);
      } 
      return null;
    }
  }
}
