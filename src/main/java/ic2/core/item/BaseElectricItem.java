package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItem;
import ic2.api.item.IItemHudInfo;
import ic2.core.IC2;
import ic2.core.init.MainConfig;
import ic2.core.ref.ItemName;
import ic2.core.util.ConfigUtil;
import ic2.core.util.LogCategory;
import java.util.LinkedList;
import java.util.List;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;

public abstract class BaseElectricItem extends ItemIC2 implements IPseudoDamageItem, IElectricItem, IItemHudInfo {
  public BaseElectricItem(ItemName name, double maxCharge, double transferLimit, int tier) {
    super(name);
    this.maxCharge = maxCharge;
    this.transferLimit = transferLimit;
    this.tier = tier;
    func_77656_e(27);
    func_77625_d(1);
    setNoRepair();
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
  
  public List<String> getHudInfo(ItemStack stack, boolean advanced) {
    List<String> info = new LinkedList<>();
    info.add(ElectricItem.manager.getToolTip(stack));
    return info;
  }
  
  public void func_150895_a(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!func_194125_a(tab))
      return; 
    ElectricItemManager.addChargeVariants(this, (List<ItemStack>)subItems);
  }
  
  public void setDamage(ItemStack stack, int damage) {
    int prev = getDamage(stack);
    if (damage != prev && logIncorrectItemDamaging)
      IC2.log.warn(LogCategory.Armor, new Throwable(), "Detected invalid armor damage application (%d):", new Object[] { Integer.valueOf(damage - prev) }); 
  }
  
  public void setStackDamage(ItemStack stack, int damage) {
    super.setDamage(stack, damage);
  }
  
  public static final boolean logIncorrectItemDamaging = ConfigUtil.getBool(MainConfig.get(), "debug/logIncorrectItemDamaging");
  
  protected final double maxCharge;
  
  protected final double transferLimit;
  
  protected final int tier;
}
