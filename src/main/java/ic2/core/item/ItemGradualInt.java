// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item;

import net.minecraft.item.Item;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import ic2.core.util.StackUtil;
import ic2.core.util.LogCategory;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import ic2.core.ref.ItemName;
import ic2.api.item.ICustomDamageItem;

public class ItemGradualInt extends ItemIC2 implements ICustomDamageItem
{
    private static final boolean alwaysShowDurability = true;
    private static final String nbtKey = "advDmg";
    private final int maxDamage;
    
    public ItemGradualInt(final ItemName name, final int maxDamage) {
        super(name);
        this.setNoRepair();
        this.maxDamage = maxDamage;
    }
    
    public boolean showDurabilityBar(final ItemStack stack) {
        return true;
    }
    
    public double getDurabilityForDisplay(final ItemStack stack) {
        return this.getCustomDamage(stack) / (double)this.getMaxCustomDamage(stack);
    }
    
    public boolean isDamageable() {
        return true;
    }
    
    public boolean isDamaged(final ItemStack stack) {
        return this.getCustomDamage(stack) > 0;
    }
    
    public int getDamage(final ItemStack stack) {
        return this.getCustomDamage(stack);
    }
    
    @Override
    public int getCustomDamage(final ItemStack stack) {
        if (!stack.hasTagCompound()) {
            return 0;
        }
        return stack.getTagCompound().getInteger("advDmg");
    }
    
    public int getMaxDamage(final ItemStack stack) {
        return this.getMaxCustomDamage(stack);
    }
    
    @Override
    public int getMaxCustomDamage(final ItemStack stack) {
        return this.maxDamage;
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int prev = this.getCustomDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid gradual item damage application (%d):", damage - prev);
        }
    }
    
    @Override
    public void setCustomDamage(final ItemStack stack, final int damage) {
        final NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
        nbt.setInteger("advDmg", damage);
    }
    
    @Override
    public boolean applyCustomDamage(final ItemStack stack, final int damage, final EntityLivingBase src) {
        this.setCustomDamage(stack, this.getCustomDamage(stack) + damage);
        return true;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        final ItemStack stack = new ItemStack((Item)this);
        this.setCustomDamage(stack, 0);
        subItems.add((Object)stack);
    }
}
