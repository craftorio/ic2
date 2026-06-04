// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.block.invslot;

import net.minecraft.item.Item;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.item.DamageHandler;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.core.block.IInventorySlotHolder;

public abstract class InvSlotConsumable extends InvSlot
{
    public InvSlotConsumable(final IInventorySlotHolder<?> base, final String name, final int count) {
        super(base, name, Access.I, count, InvSide.TOP);
    }
    
    public InvSlotConsumable(final IInventorySlotHolder<?> base, final String name, final Access access, final int count, final InvSide preferredSide) {
        super(base, name, access, count, preferredSide);
    }
    
    @Override
    public abstract boolean accepts(final ItemStack p0);
    
    @Override
    public boolean canOutput() {
        return super.canOutput() || (this.access != Access.NONE && !this.isEmpty() && !this.accepts(this.get()));
    }
    
    public ItemStack consume(final int amount) {
        return this.consume(amount, false, false);
    }
    
    public ItemStack consume(int amount, final boolean simulate, final boolean consumeContainers) {
        ItemStack ret = null;
        for (int i = 0; i < this.size(); ++i) {
            final ItemStack stack = this.get(i);
            if (StackUtil.getSize(stack) >= 1 && this.accepts(stack) && (ret == null || StackUtil.checkItemEqualityStrict(stack, ret)) && (StackUtil.getSize(stack) == 1 || consumeContainers || !stack.getItem().hasContainerItem(stack))) {
                final int currentAmount = Math.min(amount, StackUtil.getSize(stack));
                amount -= currentAmount;
                if (!simulate) {
                    if (StackUtil.getSize(stack) == currentAmount) {
                        if (!consumeContainers && stack.getItem().hasContainerItem(stack)) {
                            ItemStack container = stack.getItem().getContainerItem(stack);
                            if (container != null && container.isItemStackDamageable() && DamageHandler.getDamage(container) > DamageHandler.getMaxDamage(container)) {
                                container = null;
                            }
                            this.put(i, container);
                        }
                        else {
                            this.clear(i);
                        }
                    }
                    else {
                        this.put(i, StackUtil.decSize(stack, currentAmount));
                    }
                }
                if (ret == null) {
                    ret = StackUtil.copyWithSize(stack, currentAmount);
                }
                else {
                    ret = StackUtil.incSize(ret, currentAmount);
                }
                if (amount == 0) {
                    break;
                }
            }
        }
        return ret;
    }
    
    public int damage(final int amount, final boolean simulate) {
        return this.damage(amount, simulate, null);
    }
    
    public int damage(int amount, final boolean simulate, final EntityLivingBase src) {
        int damageApplied = 0;
        ItemStack target = null;
        for (int i = 0; i < this.size() && amount > 0; ++i) {
            ItemStack stack = this.get(i);
            if (!StackUtil.isEmpty(stack)) {
                final Item item = stack.getItem();
                if (this.accepts(stack) && item.isDamageable() && (target == null || (item == target.getItem() && ItemStack.areItemStackTagsEqual(stack, target)))) {
                    if (target == null) {
                        target = stack.copy();
                    }
                    if (simulate) {
                        stack = stack.copy();
                    }
                    final int maxDamage = DamageHandler.getMaxDamage(stack);
                    do {
                        final int currentAmount = Math.min(amount, maxDamage - DamageHandler.getDamage(stack));
                        DamageHandler.damage(stack, currentAmount, src);
                        damageApplied += currentAmount;
                        amount -= currentAmount;
                        if (DamageHandler.getDamage(stack) >= maxDamage) {
                            stack = StackUtil.decSize(stack);
                            if (StackUtil.isEmpty(stack)) {
                                break;
                            }
                            DamageHandler.setDamage(stack, 0, false);
                        }
                    } while (amount > 0 && !StackUtil.isEmpty(stack));
                    if (!simulate) {
                        this.put(i, stack);
                    }
                }
            }
        }
        return damageApplied;
    }
}
