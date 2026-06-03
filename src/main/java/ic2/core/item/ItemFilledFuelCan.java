package ic2.core.item;

import ic2.core.item.type.CraftingItemType;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.item.ItemStack;

public class ItemFilledFuelCan extends ItemIC2 {
  public ItemFilledFuelCan() {
    super(ItemName.filled_fuel_can);
    func_77625_d(1);
  }
  
  public boolean hasContainerItem(ItemStack stack) {
    return true;
  }
  
  public ItemStack getContainerItem(ItemStack stack) {
    return ItemName.crafting.getItemStack((Enum)CraftingItemType.empty_fuel_can);
  }
  
  public int getItemBurnTime(ItemStack stack) {
    return StackUtil.getOrCreateNbtData(stack).func_74762_e("value") * 2;
  }
}
