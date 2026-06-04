package ic2.core;

import ic2.core.block.TileEntityBlock;
import ic2.core.block.comp.TileEntityComponent;
import ic2.core.network.NetworkManager;
import ic2.core.slot.SlotHologramSlot;
import ic2.core.slot.SlotInvSlot;
import ic2.core.util.StackUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public abstract class ContainerBase<T extends IInventory> extends Container {
  protected static final int windowBorder = 8;
  
  protected static final int slotSize = 16;
  
  protected static final int slotDistance = 2;
  
  protected static final int slotSeparator = 4;
  
  protected static final int hotbarYOffset = -24;
  
  protected static final int inventoryYOffset = -82;
  
  public final T base;
  
  public ContainerBase(T base1) {
    this.base = base1;
  }
  
  protected void addPlayerInventorySlots(EntityPlayer player, int height) {
    addPlayerInventorySlots(player, 178, height);
  }
  
  protected void addPlayerInventorySlots(EntityPlayer player, int width, int height) {
    int xStart = (width - 162) / 2;
    for (int row = 0; row < 3; row++) {
      for (int i = 0; i < 9; i++)
        func_75146_a(new Slot((IInventory)player.field_71071_by, i + row * 9 + 9, xStart + i * 18, height + -82 + row * 18)); 
    } 
    for (int col = 0; col < 9; col++)
      func_75146_a(new Slot((IInventory)player.field_71071_by, col, xStart + col * 18, height + -24)); 
  }
  
  public ItemStack func_184996_a(int slotId, int dragType, ClickType clickType, EntityPlayer player) {
    Slot slot;
    if (slotId >= 0 && slotId < this.field_75151_b.size() && 
      slot = this.field_75151_b.get(slotId) instanceof SlotHologramSlot)
      return ((SlotHologramSlot)slot).slotClick(dragType, clickType, player); 
    return super.func_184996_a(slotId, dragType, clickType, player);
  }
  
  public final ItemStack func_82846_b(EntityPlayer player, int sourceSlotIndex) {
    Slot sourceSlot = this.field_75151_b.get(sourceSlotIndex);
    if (sourceSlot != null && sourceSlot.func_75216_d()) {
      ItemStack resultStack, sourceItemStack = sourceSlot.func_75211_c();
      int oldSourceItemStackSize = StackUtil.getSize(sourceItemStack);
      if (sourceSlot.field_75224_c == player.field_71071_by) {
        resultStack = handlePlayerSlotShiftClick(player, sourceItemStack);
      } else {
        resultStack = handleGUISlotShiftClick(player, sourceItemStack);
      } 
      if (StackUtil.isEmpty(resultStack) || StackUtil.getSize(resultStack) != oldSourceItemStackSize) {
        sourceSlot.func_75215_d(resultStack);
        sourceSlot.func_190901_a(player, sourceItemStack);
        if (!(player.func_130014_f_()).isRemote)
          func_75142_b(); 
      } 
    } 
    return StackUtil.emptyStack;
  }
  
  protected ItemStack handlePlayerSlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
    for (int run = 0; run < 4 && !StackUtil.isEmpty(sourceItemStack); run++) {
      for (Slot targetSlot : this.field_75151_b) {
        if (targetSlot.field_75224_c != player.field_71071_by)
          if (isValidTargetSlot(targetSlot, sourceItemStack, (run % 2 == 1), (run < 2))) {
            sourceItemStack = transfer(sourceItemStack, targetSlot);
            if (StackUtil.isEmpty(sourceItemStack))
              break; 
          }  
      } 
    } 
    return sourceItemStack;
  }
  
  protected ItemStack handleGUISlotShiftClick(EntityPlayer player, ItemStack sourceItemStack) {
    for (int run = 0; run < 2 && !StackUtil.isEmpty(sourceItemStack); run++) {
      for (ListIterator<Slot> it = this.field_75151_b.listIterator(this.field_75151_b.size()); it.hasPrevious(); ) {
        Slot targetSlot = it.previous();
        if (targetSlot.field_75224_c == player.field_71071_by && 
          isValidTargetSlot(targetSlot, sourceItemStack, (run == 1), false)) {
          sourceItemStack = transfer(sourceItemStack, targetSlot);
          if (StackUtil.isEmpty(sourceItemStack))
            break; 
        } 
      } 
    } 
    return sourceItemStack;
  }
  
  protected static final boolean isValidTargetSlot(Slot slot, ItemStack stack, boolean allowEmpty, boolean requireInputOnly) {
    if (slot instanceof ic2.core.slot.SlotInvSlotReadOnly || slot instanceof SlotHologramSlot)
      return false; 
    if (!slot.func_75214_a(stack))
      return false; 
    if (!allowEmpty && !slot.func_75216_d())
      return false; 
    if (requireInputOnly)
      return (slot instanceof SlotInvSlot && ((SlotInvSlot)slot).invSlot
        .canInput()); 
    return true;
  }
  
  public boolean func_75145_c(EntityPlayer entityplayer) {
    return this.base.func_70300_a(entityplayer);
  }
  
  public void func_75142_b() {
    super.func_75142_b();
    if (this.base instanceof TileEntity) {
      for (String name : getNetworkedFields()) {
        for (IContainerListener crafter : this.field_75149_d) {
          if (crafter instanceof EntityPlayerMP)
            ((NetworkManager)IC2.network.get(true)).updateTileEntityFieldTo((TileEntity)this.base, name, (EntityPlayerMP)crafter); 
        } 
      } 
      if (this.base instanceof TileEntityBlock)
        for (TileEntityComponent component : ((TileEntityBlock)this.base).getComponents()) {
          for (IContainerListener crafter : this.field_75149_d) {
            if (crafter instanceof EntityPlayerMP)
              component.onContainerUpdate((EntityPlayerMP)crafter); 
          } 
        }  
    } 
  }
  
  public List<String> getNetworkedFields() {
    return new ArrayList<>();
  }
  
  public List<IContainerListener> getListeners() {
    return this.field_75149_d;
  }
  
  public void onContainerEvent(String event) {}
  
  protected final ItemStack transfer(ItemStack stack, Slot dst) {
    int amount = getTransferAmount(stack, dst);
    if (amount <= 0)
      return stack; 
    ItemStack dstStack = dst.func_75211_c();
    if (StackUtil.isEmpty(dstStack)) {
      dst.func_75215_d(StackUtil.copyWithSize(stack, amount));
    } else {
      dst.func_75215_d(StackUtil.incSize(dstStack, amount));
    } 
    stack = StackUtil.decSize(stack, amount);
    return stack;
  }
  
  private int getTransferAmount(ItemStack stack, Slot dst) {
    int amount = Math.min(dst.field_75224_c.func_70297_j_(), dst.func_75219_a());
    amount = Math.min(amount, stack.func_77985_e() ? stack.func_77976_d() : 1);
    ItemStack dstStack = dst.func_75211_c();
    if (!StackUtil.isEmpty(dstStack)) {
      if (!StackUtil.checkItemEqualityStrict(stack, dstStack))
        return 0; 
      amount -= StackUtil.getSize(dstStack);
    } 
    amount = Math.min(amount, StackUtil.getSize(stack));
    return amount;
  }
}
