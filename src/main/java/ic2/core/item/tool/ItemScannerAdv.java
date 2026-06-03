package ic2.core.item.tool;

import ic2.api.item.ElectricItem;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class ItemScannerAdv extends ItemScanner {
  public ItemScannerAdv() {
    super(ItemName.advanced_scanner, 1000000.0D, 512.0D, 2);
  }
  
  public int startLayerScan(ItemStack stack) {
    return ElectricItem.manager.use(stack, 250.0D, null) ? (getScanRange() / 2) : 0;
  }
  
  public int getScanRange() {
    return 12;
  }
}
