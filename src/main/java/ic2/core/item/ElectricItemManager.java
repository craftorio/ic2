// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import java.util.List;
import net.minecraft.item.Item;
import java.util.Iterator;
import ic2.core.IC2;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.slot.ArmorSlot;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import ic2.core.util.Util;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.api.item.IElectricItem;
import net.minecraft.item.ItemStack;
import ic2.api.item.IElectricItemManager;

public class ElectricItemManager implements IElectricItemManager
{
    @Override
    public double charge(final ItemStack stack, double amount, final int tier, final boolean ignoreTransferLimit, final boolean simulate) {
        IElectricItem item = (IElectricItem)stack.getItem();
        assert item.getMaxCharge(stack) > 0.0;
        if (amount < 0.0 || StackUtil.getSize(stack) > 1 || item.getTier(stack) > tier) {
            return 0.0;
        }
        if (!ignoreTransferLimit && amount > item.getTransferLimit(stack)) {
            amount = item.getTransferLimit(stack);
        }
        final NBTTagCompound tNBT = StackUtil.getOrCreateNbtData(stack);
        double newCharge = tNBT.getDouble("charge");
        amount = Math.min(amount, item.getMaxCharge(stack) - newCharge);
        if (!simulate) {
            newCharge += amount;
            if (newCharge > 0.0) {
                tNBT.setDouble("charge", newCharge);
            }
            else {
                tNBT.removeTag("charge");
                if (tNBT.hasNoTags()) {
                    stack.setTagCompound((NBTTagCompound)null);
                }
            }
            if (stack.getItem() instanceof IElectricItem) {
                item = (IElectricItem)stack.getItem();
                final int maxDamage = DamageHandler.getMaxDamage(stack);
                DamageHandler.setDamage(stack, mapChargeLevelToDamage(newCharge, item.getMaxCharge(stack), maxDamage), true);
            }
            else {
                DamageHandler.setDamage(stack, 0, true);
            }
        }
        return amount;
    }
    
    private static int mapChargeLevelToDamage(final double charge, final double maxCharge, int maxDamage) {
        if (maxDamage < 2) {
            return 0;
        }
        return --maxDamage - (int)Util.map(charge, maxCharge, maxDamage);
    }
    
    @Override
    public double discharge(final ItemStack stack, double amount, final int tier, final boolean ignoreTransferLimit, final boolean externally, final boolean simulate) {
        IElectricItem item = (IElectricItem)stack.getItem();
        assert item.getMaxCharge(stack) > 0.0;
        if (amount < 0.0 || StackUtil.getSize(stack) > 1 || item.getTier(stack) > tier) {
            return 0.0;
        }
        if (externally && !item.canProvideEnergy(stack)) {
            return 0.0;
        }
        if (!ignoreTransferLimit && amount > item.getTransferLimit(stack)) {
            amount = item.getTransferLimit(stack);
        }
        final NBTTagCompound tNBT = StackUtil.getOrCreateNbtData(stack);
        double newCharge = tNBT.getDouble("charge");
        amount = Math.min(amount, newCharge);
        if (!simulate) {
            newCharge -= amount;
            if (newCharge > 0.0) {
                tNBT.setDouble("charge", newCharge);
            }
            else {
                tNBT.removeTag("charge");
                if (tNBT.hasNoTags()) {
                    stack.setTagCompound((NBTTagCompound)null);
                }
            }
            if (stack.getItem() instanceof IElectricItem) {
                item = (IElectricItem)stack.getItem();
                final int maxDamage = DamageHandler.getMaxDamage(stack);
                DamageHandler.setDamage(stack, mapChargeLevelToDamage(newCharge, item.getMaxCharge(stack), maxDamage), true);
            }
            else {
                DamageHandler.setDamage(stack, 0, true);
            }
        }
        return amount;
    }
    
    @Override
    public double getCharge(final ItemStack stack) {
        return ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, false, true);
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) + ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
    }
    
    @Override
    public boolean canUse(final ItemStack stack, final double amount) {
        return ElectricItem.manager.getCharge(stack) >= amount;
    }
    
    @Override
    public boolean use(final ItemStack stack, final double amount, final EntityLivingBase entity) {
        if (entity != null) {
            ElectricItem.manager.chargeFromArmor(stack, entity);
        }
        final double transfer = ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, true);
        if (Util.isSimilar(transfer, amount)) {
            ElectricItem.manager.discharge(stack, amount, Integer.MAX_VALUE, true, false, false);
            if (entity != null) {
                ElectricItem.manager.chargeFromArmor(stack, entity);
            }
            return true;
        }
        return false;
    }
    
    @Override
    public void chargeFromArmor(final ItemStack target, final EntityLivingBase entity) {
        boolean transferred = false;
        for (final EntityEquipmentSlot slot : ArmorSlot.getAll()) {
            final ItemStack source = entity.getItemStackFromSlot(slot);
            if (source == null) {
                continue;
            }
            int tier;
            if (source.getItem() instanceof IElectricItem) {
                tier = ((IElectricItem)source.getItem()).getTier(target);
            }
            else {
                tier = Integer.MAX_VALUE;
            }
            double transfer = ElectricItem.manager.discharge(source, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true, true);
            if (transfer <= 0.0) {
                continue;
            }
            transfer = ElectricItem.manager.charge(target, transfer, tier, true, false);
            if (transfer <= 0.0) {
                continue;
            }
            ElectricItem.manager.discharge(source, transfer, Integer.MAX_VALUE, true, true, false);
            transferred = true;
        }
        if (transferred && entity instanceof EntityPlayer && IC2.platform.isSimulating()) {
            ((EntityPlayer)entity).openContainer.detectAndSendChanges();
        }
    }
    
    @Override
    public String getToolTip(final ItemStack stack) {
        final double charge = ElectricItem.manager.getCharge(stack);
        final double space = ElectricItem.manager.charge(stack, Double.POSITIVE_INFINITY, Integer.MAX_VALUE, true, true);
        return Util.toSiString(charge, 3) + "/" + Util.toSiString(charge + space, 3) + " EU";
    }
    
    public static ItemStack getCharged(final Item item, final double charge) {
        if (!(item instanceof IElectricItem)) {
            throw new IllegalArgumentException("no electric item");
        }
        final ItemStack ret = new ItemStack(item);
        ElectricItem.manager.charge(ret, charge, Integer.MAX_VALUE, true, false);
        return ret;
    }
    
    public static void addChargeVariants(final Item item, final List<ItemStack> list) {
        list.add(getCharged(item, 0.0));
        list.add(getCharged(item, Double.POSITIVE_INFINITY));
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof IElectricItem)) {
            return 0;
        }
        return ((IElectricItem)stack.getItem()).getTier(stack);
    }
}
