// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import ic2.core.util.LogCategory;
import ic2.core.IC2;
import ic2.core.item.BaseElectricItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.Item;
import ic2.core.item.ElectricItemManager;
import net.minecraft.util.NonNullList;
import net.minecraft.creativetab.CreativeTabs;
import ic2.core.init.Localization;
import ic2.api.item.ElectricItem;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import ic2.api.item.IItemHudInfo;
import ic2.api.item.IElectricItem;
import ic2.core.item.IPseudoDamageItem;
import net.minecraftforge.common.ISpecialArmor;

public abstract class ItemArmorElectric extends ItemArmorIC2 implements ISpecialArmor, IPseudoDamageItem, IElectricItem, IItemHudInfo
{
    protected final double maxCharge;
    protected final double transferLimit;
    protected final int tier;
    
    public ItemArmorElectric(final ItemName name, final String armorName, final EntityEquipmentSlot armorType, final double maxCharge, final double transferLimit, final int tier) {
        super(name, ItemArmor.ArmorMaterial.DIAMOND, armorName, armorType, null);
        this.maxCharge = maxCharge;
        this.tier = tier;
        this.transferLimit = transferLimit;
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }
    
    public int getItemEnchantability() {
        return 0;
    }
    
    public boolean isEnchantable(final ItemStack stack) {
        return false;
    }
    
    public boolean isBookEnchantable(final ItemStack stack, final ItemStack book) {
        return false;
    }
    
    public List<String> getHudInfo(final ItemStack stack, final boolean advanced) {
        final List<String> info = new LinkedList<String>();
        info.add(ElectricItem.manager.getToolTip(stack));
        info.add(Localization.translate("ic2.item.tooltip.PowerTier", this.tier));
        return info;
    }
    
    public void getSubItems(final CreativeTabs tab, final NonNullList<ItemStack> subItems) {
        if (!this.isInCreativeTab(tab)) {
            return;
        }
        ElectricItemManager.addChargeVariants((Item)this, (List<ItemStack>)subItems);
    }
    
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        if (source.isUnblockable()) {
            return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
        }
        final double absorptionRatio = this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio();
        final int energyPerDamage = this.getEnergyPerDamage();
        int damageLimit = Integer.MAX_VALUE;
        if (energyPerDamage > 0) {
            damageLimit = (int)Math.min(damageLimit, 25.0 * ElectricItem.manager.getCharge(armor) / energyPerDamage);
        }
        return new ISpecialArmor.ArmorProperties(0, absorptionRatio, damageLimit);
    }
    
    public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
        if (ElectricItem.manager.getCharge(armor) >= this.getEnergyPerDamage()) {
            return (int)Math.round(20.0 * this.getBaseAbsorptionRatio() * this.getDamageAbsorptionRatio());
        }
        return 0;
    }
    
    public void damageArmor(final EntityLivingBase entity, final ItemStack stack, final DamageSource source, final int damage, final int slot) {
        ElectricItem.manager.discharge(stack, damage * this.getEnergyPerDamage(), Integer.MAX_VALUE, true, false, false);
    }
    
    public boolean canProvideEnergy(final ItemStack stack) {
        return false;
    }
    
    public double getMaxCharge(final ItemStack stack) {
        return this.maxCharge;
    }
    
    public int getTier(final ItemStack stack) {
        return this.tier;
    }
    
    public double getTransferLimit(final ItemStack stack) {
        return this.transferLimit;
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {
        return false;
    }
    
    public void setDamage(final ItemStack stack, final int damage) {
        final int prev = this.getDamage(stack);
        if (damage != prev && BaseElectricItem.logIncorrectItemDamaging) {
            IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", damage - prev);
        }
    }
    
    public void setStackDamage(final ItemStack stack, final int damage) {
        super.setDamage(stack, damage);
    }
    
    public abstract double getDamageAbsorptionRatio();
    
    public abstract int getEnergyPerDamage();
    
    protected final double getBaseAbsorptionRatio() {
        switch (this.armorType) {
            case HEAD: {
                return 0.15;
            }
            case CHEST: {
                return 0.4;
            }
            case LEGS: {
                return 0.3;
            }
            case FEET: {
                return 0.15;
            }
            default: {
                return 0.0;
            }
        }
    }
}
