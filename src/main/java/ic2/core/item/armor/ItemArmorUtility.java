package ic2.core.item.armor;

import ic2.core.ref.ItemName;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraftforge.common.ISpecialArmor;

public class ItemArmorUtility extends ItemArmorIC2 implements ISpecialArmor {
  public ItemArmorUtility(ItemName name, String armorName, EntityEquipmentSlot type) {
    super(name, ItemArmor.ArmorMaterial.DIAMOND, armorName, type, null);
  }
  
  public int func_77619_b() {
    return 0;
  }
  
  public boolean func_82789_a(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    return false;
  }
  
  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    return new ISpecialArmor.ArmorProperties(0, 0.0D, 0);
  }
  
  public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
    return 0;
  }
  
  public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {}
}
