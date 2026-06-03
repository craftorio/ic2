package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.Localization;
import ic2.core.item.BaseElectricItem;
import ic2.core.item.ElectricItemManager;
import ic2.core.item.IPseudoDamageItem;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.DamageSource;
import net.minecraft.util.NonNullList;
import net.minecraftforge.common.ISpecialArmor;

public abstract class ItemArmorElectric extends ItemArmorIC2 implements ISpecialArmor, IPseudoDamageItem, IElectricItem, IItemHudInfo {
  protected final double maxCharge;
  
  protected final double transferLimit;
  
  protected final int tier;
  
  public ItemArmorElectric(ItemName name, String armorName, EntityEquipmentSlot armorType, double maxCharge, double transferLimit, int tier) {
    super(name, ItemArmor.ArmorMaterial.DIAMOND, armorName, armorType, (Object)null);
    this.maxCharge = maxCharge;
    this.tier = tier;
    this.transferLimit = transferLimit;
    func_77656_e(27);
    func_77625_d(1);
    setNoRepair();
  }
  
  public int func_77619_b() {
    return 0;
  }
  
  public boolean func_77616_k(ItemStack stack) {
    return false;
  }
  
  public boolean isBookEnchantable(ItemStack stack, ItemStack book) {
    return false;
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    info.add(Localization.translate("ic2.item.tooltip.PowerTier", new Object[] { Integer.valueOf(this.tier) }));
    return info;
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    ElectricItemManager.addChargeVariants((Item)this, (List)subItems);
  }
  
  public ISpecialArmor.ArmorProperties getProperties(EntityLivingBase player, ItemStack armor, DamageSource source, double damage, int slot) {
    if (source.func_76363_c())
      return new ISpecialArmor.ArmorProperties(0, 0.0D, 0); 
    double absorptionRatio = getBaseAbsorptionRatio() * getDamageAbsorptionRatio();
    int energyPerDamage = getEnergyPerDamage();
    int damageLimit = Integer.MAX_VALUE;
    if (energyPerDamage > 0)
      damageLimit = (int)Math.min(damageLimit, 25.0D * ElectricItem.manager.getCharge(armor) / energyPerDamage); 
    return new ISpecialArmor.ArmorProperties(0, absorptionRatio, damageLimit);
  }
  
  public int getArmorDisplay(EntityPlayer player, ItemStack armor, int slot) {
    if (ElectricItem.manager.getCharge(armor) >= getEnergyPerDamage())
      return (int)Math.round(20.0D * getBaseAbsorptionRatio() * getDamageAbsorptionRatio()); 
    return 0;
  }
  
  public void damageArmor(EntityLivingBase entity, ItemStack stack, DamageSource source, int damage, int slot) {
    ElectricItem.manager.discharge(stack, (damage * getEnergyPerDamage()), 2147483647, true, false, false);
  }
  
  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }
  
  public double getMaxCharge(ItemStack stack) {
    return this.maxCharge;
  }
  
  public int getTier(ItemStack stack) {
    return this.tier;
  }
  
  public double getTransferLimit(ItemStack stack) {
    return this.transferLimit;
  }
  
  public boolean func_82789_a(ItemStack par1ItemStack, ItemStack par2ItemStack) {
    return false;
  }
  
  public void setDamage(ItemStack stack, int damage) {
    int prev = getDamage(stack);
    if (damage != prev && BaseElectricItem.logIncorrectItemDamaging)
      IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", new Object[] { Integer.valueOf(damage - prev) }); 
  }
  
  public void setStackDamage(ItemStack stack, int damage) {
    super.setDamage(stack, damage);
  }
  
  public abstract double getDamageAbsorptionRatio();
  
  public abstract int getEnergyPerDamage();
  
  protected final double getBaseAbsorptionRatio() {
    switch (this.field_77881_a) {
      case HEAD:
        return 0.15D;
      case CHEST:
        return 0.4D;
      case LEGS:
        return 0.3D;
      case FEET:
        return 0.15D;
    } 
    return 0.0D;
  }
}
