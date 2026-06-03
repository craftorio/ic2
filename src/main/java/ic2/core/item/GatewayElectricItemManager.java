package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.api.item.IElectricItemManager;
import ic2.api.item.ISpecialElectricItem;
import ic2.core.util.StackUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class GatewayElectricItemManager implements IElectricItemManager {
  public double charge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return 0.0D; 
    return manager.charge(stack, amount, tier, ignoreTransferLimit, simulate);
  }
  
  public double discharge(ItemStack stack, double amount, int tier, boolean ignoreTransferLimit, boolean externally, boolean simulate) {
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return 0.0D; 
    return manager.discharge(stack, amount, tier, ignoreTransferLimit, externally, simulate);
  }
  
  public double getCharge(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return 0.0D; 
    return manager.getCharge(stack);
  }
  
  public double getMaxCharge(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return 0.0D; 
    return manager.getMaxCharge(stack);
  }
  
  public boolean canUse(ItemStack stack, double amount) {
    if (StackUtil.isEmpty(stack))
      return false; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return false; 
    return manager.canUse(stack, amount);
  }
  
  public boolean use(ItemStack stack, double amount, EntityLivingBase entity) {
    if (StackUtil.isEmpty(stack))
      return false; 
    if (entity instanceof EntityPlayer && ((EntityPlayer)entity).field_71075_bZ.field_75098_d)
      return canUse(stack, amount); 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return false; 
    return manager.use(stack, amount, entity);
  }
  
  public void chargeFromArmor(ItemStack stack, EntityLivingBase entity) {
    if (StackUtil.isEmpty(stack))
      return; 
    if (entity == null)
      return; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return; 
    manager.chargeFromArmor(stack, entity);
  }
  
  public String getToolTip(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return null; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return null; 
    return manager.getToolTip(stack);
  }
  
  private IElectricItemManager getManager(ItemStack stack) {
    Item item = stack.func_77973_b();
    if (item == null)
      return null; 
    if (item instanceof ISpecialElectricItem)
      return ((ISpecialElectricItem)item).getManager(stack); 
    if (item instanceof ic2.api.item.IElectricItem)
      return ElectricItem.rawManager; 
    return (IElectricItemManager)ElectricItem.getBackupManager(stack);
  }
  
  public int getTier(ItemStack stack) {
    if (StackUtil.isEmpty(stack))
      return 0; 
    IElectricItemManager manager = getManager(stack);
    if (manager == null)
      return 0; 
    return manager.getTier(stack);
  }
}
