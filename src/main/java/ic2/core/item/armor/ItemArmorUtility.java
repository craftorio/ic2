// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.item.armor;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor;
import net.minecraft.inventory.EntityEquipmentSlot;
import ic2.core.ref.ItemName;
import net.minecraftforge.common.ISpecialArmor;

public class ItemArmorUtility extends ItemArmorIC2 implements ISpecialArmor
{
    public ItemArmorUtility(final ItemName name, final String armorName, final EntityEquipmentSlot type) {
        super(name, ItemArmor.ArmorMaterial.DIAMOND, armorName, type, null);
    }
    
    public int getItemEnchantability() {
        return 0;
    }
    
    @Override
    public boolean getIsRepairable(final ItemStack par1ItemStack, final ItemStack par2ItemStack) {
        return false;
    }
    
    public ISpecialArmor.ArmorProperties getProperties(final EntityLivingBase player, final ItemStack armor, final DamageSource source, final double damage, final int slot) {
        return new ISpecialArmor.ArmorProperties(0, 0.0, 0);
    }
    
    public int getArmorDisplay(final EntityPlayer player, final ItemStack armor, final int slot) {
        return 0;
    }
    
    public void damageArmor(final EntityLivingBase entity, final ItemStack stack, final DamageSource source, final int damage, final int slot) {
    }
}
