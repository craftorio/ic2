package ic2.core.block.invslot;

import ic2.api.energy.tile.IDischargingSlot;
import ic2.api.info.Info;
import ic2.api.item.ElectricItem;
import ic2.core.block.IInventorySlotHolder;
import ic2.core.util.StackUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class InvSlotDischarge extends InvSlot implements IDischargingSlot {
  public int tier;
  
  public boolean allowRedstoneDust;
  
  public InvSlotDischarge(IInventorySlotHolder<?> base, InvSlot.Access access, int tier) {
    this(base, access, tier, InvSlot.InvSide.ANY);
  }
  
  public InvSlotDischarge(IInventorySlotHolder<?> base, InvSlot.Access access, int tier, InvSlot.InvSide preferredSide) {
    this(base, access, tier, true, preferredSide);
  }
  
  public InvSlotDischarge(IInventorySlotHolder<?> base, InvSlot.Access access, int tier, boolean allowRedstoneDust, InvSlot.InvSide preferredSide) {
    super(base, "discharge", access, 1, preferredSide);
    this.allowRedstoneDust = true;
    this.tier = tier;
    this.allowRedstoneDust = allowRedstoneDust;
  }
  
  public boolean accepts(ItemStack stack) {
    if (stack == null)
      return false; 
    if (stack.getItem() == Items.field_151137_ax && !this.allowRedstoneDust)
      return false; 
    return (Info.itemInfo.getEnergyValue(stack) > 0.0D || ElectricItem.manager.discharge(stack, Double.POSITIVE_INFINITY, this.tier, true, true, true) > 0.0D);
  }
  
  public double discharge(double amount, boolean ignoreLimit) {
    if (amount <= 0.0D)
      throw new IllegalArgumentException("Amount must be > 0."); 
    ItemStack stack = get(0);
    if (StackUtil.isEmpty(stack))
      return 0.0D; 
    double realAmount = ElectricItem.manager.discharge(stack, amount, this.tier, ignoreLimit, true, false);
    if (realAmount <= 0.0D) {
      realAmount = Info.itemInfo.getEnergyValue(stack);
      if (realAmount <= 0.0D)
        return 0.0D; 
      put(0, StackUtil.decSize(stack));
    } 
    return realAmount;
  }
  
  public void setTier(int tier1) {
    this.tier = tier1;
  }
}
