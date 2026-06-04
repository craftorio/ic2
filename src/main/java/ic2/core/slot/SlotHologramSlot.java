package ic2.core.slot;

import ic2.core.util.StackUtil;
import ic2.core.util.Util;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class SlotHologramSlot extends Slot {
  protected final ItemStack[] stacks;
  
  protected final int index;
  
  protected final int stackSizeLimit;
  
  protected final ChangeCallback changeCallback;
  
  public SlotHologramSlot(ItemStack[] stacks, int index, int x, int y, int stackSizeLimit, ChangeCallback changeCallback) {
    super(null, 0, x, y);
    if (index >= stacks.length)
      throw new ArrayIndexOutOfBoundsException(index); 
    this.stacks = stacks;
    this.index = index;
    this.stackSizeLimit = stackSizeLimit;
    this.changeCallback = changeCallback;
  }
  
  public boolean func_82869_a(EntityPlayer player) {
    return false;
  }
  
  public int func_75219_a() {
    return this.stackSizeLimit;
  }
  
  public boolean func_75214_a(ItemStack stack) {
    return false;
  }
  
  public ItemStack func_75211_c() {
    return StackUtil.wrapEmpty(this.stacks[this.index]);
  }
  
  public void func_75215_d(ItemStack stack) {
    this.stacks[this.index] = stack;
  }
  
  public void func_75218_e() {
    if (Util.inDev())
      System.out.println(StackUtil.toStringSafe(this.stacks)); 
    if (this.changeCallback != null)
      this.changeCallback.onChanged(this.index); 
  }
  
  public ItemStack func_75209_a(int amount) {
    return StackUtil.emptyStack;
  }
  
  public boolean func_75217_a(IInventory inventory, int index) {
    return false;
  }
  
  public ItemStack slotClick(int dragType, ClickType clickType, EntityPlayer player) {
    if (Util.inDev() && (player.getEntityWorld()).isRemote)
      System.out.printf("dragType=%d clickType=%s stack=%s%n", new Object[] { Integer.valueOf(dragType), clickType, player.inventory.func_70445_o() }); 
    if (clickType == ClickType.PICKUP && (dragType == 0 || dragType == 1)) {
      ItemStack playerStack = player.inventory.func_70445_o();
      ItemStack slotStack = this.stacks[this.index];
      if (!StackUtil.isEmpty(playerStack)) {
        int curSize = StackUtil.getSize(slotStack);
        int extraSize = (dragType == 0) ? StackUtil.getSize(playerStack) : 1;
        int limit = Math.min(playerStack.func_77976_d(), this.stackSizeLimit);
        if (curSize + extraSize > limit)
          extraSize = Math.max(0, limit - curSize); 
        if (curSize == 0) {
          this.stacks[this.index] = StackUtil.copyWithSize(playerStack, extraSize);
        } else if (StackUtil.checkItemEquality(playerStack, slotStack)) {
          if (Util.inDev())
            System.out.println("add " + extraSize + " to " + slotStack + " -> " + (curSize + extraSize)); 
          this.stacks[this.index] = StackUtil.incSize(slotStack, extraSize);
        } else {
          this.stacks[this.index] = StackUtil.copyWithSize(playerStack, Math.min(StackUtil.getSize(playerStack), limit));
        } 
      } else if (!StackUtil.isEmpty(slotStack)) {
        if (dragType == 0) {
          this.stacks[this.index] = StackUtil.emptyStack;
        } else {
          int newSize = StackUtil.getSize(slotStack) / 2;
          if (newSize <= 0) {
            this.stacks[this.index] = StackUtil.emptyStack;
          } else {
            this.stacks[this.index] = StackUtil.setSize(slotStack, newSize);
          } 
        } 
      } 
      func_75218_e();
    } 
    return StackUtil.emptyStack;
  }
  
  public static interface ChangeCallback {
    void onChanged(int param1Int);
  }
}
