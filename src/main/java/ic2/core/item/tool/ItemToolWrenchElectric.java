// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.tool;

import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.item.BaseElectricItem;
import net.minecraft.item.Item;
import ic2.core.item.ElectricItemManager;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.item.ElectricItem;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IElectricItem;
import ic2.core.item.IPseudoDamageItem;

public class ItemToolWrenchElectric extends ItemToolWrench implements IPseudoDamageItem, IElectricItem, IItemHudInfo
{
    public ItemToolWrenchElectric() {
        super(ItemName.electric_wrench);
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }
    
    @Override
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        return info;
    }
    
    @Override
    public boolean canTakeDamage(final ItemStack stack, int amount) {
        amount *= 100;
        return ElectricItem.manager.getCharge(stack) >= amount;
    }
    
    @Override
    public void damage(final ItemStack stack, final int amount, final EntityPlayer player) {
        ElectricItem.manager.use(stack, 100 * amount, (EntityLivingBase)player);
    }
    
    @Override
    public boolean canProvideEnergy(final ItemStack stack) {
        return false;
    }
    
    @Override
    public double getMaxCharge(final ItemStack stack) {
        return 12000.0;
    }
    
    @Override
    public int getTier(final ItemStack stack) {
        return 1;
    }
    
    @Override
    public double getTransferLimit(final ItemStack stack) {
        return 250.0;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ElectricItemManager.addChargeVariants(this, (List<ItemStack>)subItems);
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack toRepair, final ItemStack repair) {
        return false;
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int prev = this.getDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
        }
    }
    
    @Override
    public void setStackDamage(final ItemStack stack, final int damage) {
        super.setDamage(stack, damage);
    }
}
