package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public class ItemToolWrenchElectric extends ItemToolWrench implements IPseudoDamageItem, IElectricItem, IItemHudInfo {
  public ItemToolWrenchElectric() {
    super(ItemName.electric_wrench);
    func_77656_e(27);
    func_77625_d(1);
    setNoRepair();
  }
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    return info;
  }
  
  public boolean canTakeDamage(ItemStack stack, int amount) {
    amount *= 100;
    return (ElectricItem.manager.getCharge(stack) >= amount);
  }
  
  public void damage(ItemStack stack, int amount, EntityPlayer player) {
    ElectricItem.manager.use(stack, (100 * amount), (EntityLivingBase)player);
  }
  
  public boolean canProvideEnergy(ItemStack stack) {
    return false;
  }
  
  public double getMaxCharge(ItemStack stack) {
    return 12000.0D;
  }
  
  public int getTier(ItemStack stack) {
    return 1;
  }
  
  public double getTransferLimit(ItemStack stack) {
    return 250.0D;
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    ElectricItemManager.addChargeVariants((Item)this, (List)subItems);
  }
  
  public boolean func_82789_a(ItemStack toRepair, ItemStack repair) {
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
}
