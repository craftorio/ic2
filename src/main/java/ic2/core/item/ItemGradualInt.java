package ic2.core.item;

import ic2.api.item.ICustomDamageItem;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.LogCategory;
import ic2.core.util.StackUtil;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class ItemGradualInt extends ItemIC2 implements ICustomDamageItem {
  private static final boolean alwaysShowDurability = true;
  
  private static final String nbtKey = "advDmg";
  
  private final int maxDamage;
  
  public ItemGradualInt(ItemName name, int maxDamage) {
    super(name);
    setNoRepair();
    this.maxDamage = maxDamage;
  }
  
  public boolean showDurabilityBar(ItemStack stack) {
    return true;
  }
  
  public double getDurabilityForDisplay(ItemStack stack) {
    return getCustomDamage(stack) / getMaxCustomDamage(stack);
  }
  
  public boolean isDamageable() {
    return true;
  }
  
  public boolean isDamaged(ItemStack stack) {
    return (getCustomDamage(stack) > 0);
  }
  
  public int getDamage(ItemStack stack) {
    return getCustomDamage(stack);
  }
  
  public int getCustomDamage(ItemStack stack) {
    if (!stack.hasTagCompound())
      return 0; 
    return stack.getTagCompound().getInteger("advDmg");
  }
  
  public int getMaxDamage(ItemStack stack) {
    return getMaxCustomDamage(stack);
  }
  
  public int getMaxCustomDamage(ItemStack stack) {
    return this.maxDamage;
  }
  
  public void setDamage(ItemStack stack, int damage) {
    int prev = getCustomDamage(stack);
    if (damage != prev && BaseElectricItem.logIncorrectItemDamaging)
      IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid gradual item damage application (%d):", new Object[] { Integer.valueOf(damage - prev) }); 
  }
  
  public void setCustomDamage(ItemStack stack, int damage) {
    NBTTagCompound nbt = StackUtil.getOrCreateNbtData(stack);
    nbt.setInteger("advDmg", damage);
  }
  
  public boolean applyCustomDamage(ItemStack stack, int damage, EntityLivingBase src) {
    setCustomDamage(stack, getCustomDamage(stack) + damage);
    return true;
  }
  
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!isInCreativeTab(tab))
      return; 
    ItemStack stack = new ItemStack(this);
    setCustomDamage(stack, 0);
    subItems.add(stack);
  }
}
