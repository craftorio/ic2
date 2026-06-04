// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.item.Item;
import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.ISpecialElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;
import ic2.api.item.IElectricItemManager;

public class GatewayElectricItemManager implements IElectricItemManager
{
    @Override
    public double charge(final ItemStack stack, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean simulate) {
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return 0.0;
        }
        return manager.charge(stack, amount, tier, ignoreTransferLimit, simulate);
    }
    
    @Override
    public double discharge(final ItemStack stack, final double amount, final int tier, final boolean ignoreTransferLimit, final boolean externally, final boolean simulate) {
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return 0.0;
        }
        return manager.discharge(stack, amount, tier, ignoreTransferLimit, externally, simulate);
    }
    
    @Override
    public double getCharge(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return 0.0;
        }
        return manager.getCharge(stack);
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0.0;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return 0.0;
        }
        return manager.getMaxCharge(stack);
    }
    
    @Override
    public boolean canUse(final ItemStack stack, final double amount) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        final IElectricItemManager manager = this.getManager(stack);
        return manager != null && manager.canUse(stack, amount);
    }
    
    @Override
    public boolean use(final ItemStack stack, final double amount, final EntityLivingBase entity) {
        if (StackUtil.isEmpty(stack)) {
            return false;
        }
        if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode) {
            return this.canUse(stack, amount);
        }
        final IElectricItemManager manager = this.getManager(stack);
        return manager != null && manager.use(stack, amount, entity);
    }
    
    @Override
    public void chargeFromArmor(final ItemStack stack, final EntityLivingBase entity) {
        if (StackUtil.isEmpty(stack)) {
            return;
        }
        if (entity == null) {
            return;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return;
        }
        manager.chargeFromArmor(stack, entity);
    }
    
    @Override
    public String getToolTip(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return null;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return null;
        }
        return manager.getToolTip(stack);
    }
    
    private IElectricItemManager getManager(final ItemStack stack) {
        final Item item = stack.getItem();
        if (item == null) {
            return null;
        }
        if (item instanceof ISpecialElectricItem) {
            return ((ISpecialElectricItem)item).getManager(stack);
        }
        if (item instanceof IElectricItem) {
            return ElectricItem.rawManager;
        }
        return ElectricItem.getBackupManager(stack);
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        if (StackUtil.isEmpty(stack)) {
            return 0;
        }
        final IElectricItemManager manager = this.getManager(stack);
        if (manager == null) {
            return 0;
        }
        return manager.getTier(stack);
    }
}
