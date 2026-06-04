package ic2.core.item.armor;

import ic2.core.item.armor.jetpack.IJetpack;
import ic2.core.ref.FluidName;
import ic2.core.ref.ItemName;
import ic2.core.util.Util;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;

public class ItemArmorJetpack extends ItemArmorFluidTank implements IJetpack {
  public ItemArmorJetpack() {
    super(ItemName.jetpack, "jetpack", FluidName.biogas.getInstance(), 30000);
  }
  
  public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
    if (!isInCreativeTab(tab))
      return; 
    ItemStack stack = new ItemStack((Item)this, 1);
    filltank(stack);
    stack.setItemDamage(1);
    subItems.add(stack);
    stack = new ItemStack((Item)this, 1);
    stack.setItemDamage(getMaxDamage(stack));
    subItems.add(stack);
  }
  
  public boolean drainEnergy(ItemStack pack, int amount) {
    if (isEmpty(pack))
      return false; 
    IFluidHandlerItem handler = FluidUtil.getFluidHandler(pack);
    assert handler != null;
    FluidStack drained = handler.drain(amount, false);
    if (drained == null || drained.amount < amount)
      return false; 
    handler.drain(amount, true);
    Updatedamage(pack);
    return true;
  }
  
  public float getPower(ItemStack stack) {
    return 1.0F;
  }
  
  public float getDropPercentage(ItemStack stack) {
    return 0.2F;
  }
  
  public boolean isJetpackActive(ItemStack stack) {
    return true;
  }
  
  public double getChargeLevel(ItemStack stack) {
    return getCharge(stack) / getMaxCharge(stack);
  }
  
  public float getHoverMultiplier(ItemStack stack, boolean upwards) {
    return 0.2F;
  }
  
  public float getWorldHeightDivisor(ItemStack stack) {
    return 1.0F;
  }
  
  public int getBarPercent(ItemStack stack) {
    return (int)Util.map(getCharge(stack), getMaxCharge(stack), 100.0D);
  }
}
