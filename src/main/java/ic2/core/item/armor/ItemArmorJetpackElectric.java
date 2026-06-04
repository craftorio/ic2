// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import ic2.api.item.ElectricItem;
import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import ic2.core.item.armor.jetpack.IJetpack;

public class ItemArmorJetpackElectric extends ItemArmorElectric implements IJetpack
{
    public ItemArmorJetpackElectric() {
        super(ItemName.jetpack_electric, "jetpack", EntityEquipmentSlot.CHEST, 30000.0, 60.0, 1);
        this.setMaxDamage(27);
        this.setMaxStackSize(1);
        this.setNoRepair();
    }
    
    @Override
    public boolean drainEnergy(final ItemStack pack, final int amount) {
        return ElectricItem.manager.discharge(pack, amount + 6, Integer.MAX_VALUE, true, false, false) > 0.0;
    }
    
    @Override
    public float getPower(final ItemStack stack) {
        return 0.7f;
    }
    
    @Override
    public float getDropPercentage(final ItemStack stack) {
        return 0.05f;
    }
    
    @Override
    public boolean isJetpackActive(final ItemStack stack) {
        return true;
    }
    
    @Override
    public double getChargeLevel(final ItemStack stack) {
        return ElectricItem.manager.getCharge(stack) / this.getMaxCharge(stack);
    }
    
    @Override
    public float getHoverMultiplier(final ItemStack stack, final boolean upwards) {
        return 0.1f;
    }
    
    @Override
    public float getWorldHeightDivisor(final ItemStack stack) {
        return 1.28f;
    }
    
    @Override
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
    }
    
    @Override
    public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
        return 0;
    }
    
    @Override
    public int getEnergyPerDamage() {
        return 0;
    }
    
    @Override
    public double getDamageAbsorptionRatio() {
        return 0.0;
    }
}
