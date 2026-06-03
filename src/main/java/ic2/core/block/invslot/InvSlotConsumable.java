package ic2.core.block.invslot;

import ic2.core.block.IInventorySlotHolder;
import ic2.core.item.DamageHandler;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public abstract class InvSlotConsumable extends InvSlot {
  public InvSlotConsumable(IInventorySlotHolder<?> base, String name, int count) {
    super(base, name, InvSlot.Access.I, count, InvSlot.InvSide.TOP);
  }
  
  public InvSlotConsumable(IInventorySlotHolder<?> base, String name, InvSlot.Access access, int count, InvSlot.InvSide preferredSide) {
    super(base, name, access, count, preferredSide);
  }
  
  public abstract boolean accepts(ItemStack paramItemStack);
  
  public boolean canOutput() {
    return (super.canOutput() || (this.access != InvSlot.Access.NONE && 
      !isEmpty() && !accepts(get())));
  }
  
  public ItemStack consume(int amount) {
    return consume(amount, false, false);
  }
  
  public ItemStack consume(int amount, boolean simulate, boolean consumeContainers) {
    ItemStack ret = null;
    for (int i = 0; i < size(); i++) {
      ItemStack stack = get(i);
      if (StackUtil.getSize(stack) >= 1 && 
        accepts(stack) && (ret == null || 
        StackUtil.checkItemEqualityStrict(stack, ret)) && (
        StackUtil.getSize(stack) == 1 || consumeContainers || !stack.func_77973_b().hasContainerItem(stack))) {
        int currentAmount = Math.min(amount, StackUtil.getSize(stack));
        amount -= currentAmount;
        if (!simulate)
          if (StackUtil.getSize(stack) == currentAmount) {
            if (!consumeContainers && stack.func_77973_b().hasContainerItem(stack)) {
              ItemStack container = stack.func_77973_b().getContainerItem(stack);
              if (container != null && container.func_77984_f() && DamageHandler.getDamage(container) > DamageHandler.getMaxDamage(container))
                container = null; 
              put(i, container);
            } else {
              clear(i);
            } 
          } else {
            put(i, StackUtil.decSize(stack, currentAmount));
          }  
        if (ret == null) {
          ret = StackUtil.copyWithSize(stack, currentAmount);
        } else {
          ret = StackUtil.incSize(ret, currentAmount);
        } 
        if (amount == 0)
          break; 
      } 
    } 
    return ret;
  }
  
  public int damage(int amount, boolean simulate) {
    return damage(amount, simulate, (EntityLivingBase)null);
  }
  
  public int damage(int amount, boolean simulate, EntityLivingBase src) {
    int damageApplied = 0;
    ItemStack target = null;
    for (int i = 0; i < size() && amount > 0; i++) {
      ItemStack stack = get(i);
      if (!StackUtil.isEmpty(stack)) {
        Item item = stack.func_77973_b();
        if (accepts(stack) && item.func_77645_m() && (target == null || (item == target
          .func_77973_b() && ItemStack.func_77970_a(stack, target)))) {
          if (target == null)
            target = stack.func_77946_l(); 
          if (simulate)
            stack = stack.func_77946_l(); 
          int maxDamage = DamageHandler.getMaxDamage(stack);
          do {
            int currentAmount = Math.min(amount, maxDamage - DamageHandler.getDamage(stack));
            DamageHandler.damage(stack, currentAmount, src);
            damageApplied += currentAmount;
            amount -= currentAmount;
            if (DamageHandler.getDamage(stack) < maxDamage)
              continue; 
            stack = StackUtil.decSize(stack);
            if (!StackUtil.isEmpty(stack)) {
              DamageHandler.setDamage(stack, 0, false);
            } else {
              break;
            } 
          } while (amount > 0 && !StackUtil.isEmpty(stack));
          if (!simulate)
            put(i, stack); 
        } 
      } 
    } 
    return damageApplied;
  }
}
