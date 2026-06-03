package ic2.core.item.armor;

import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemArmorBatpack extends ItemArmorElectric {
  public ItemArmorBatpack() {
    super(ItemName.batpack, "batpack", EntityEquipmentSlot.CHEST, 60000.0D, 100.0D, 1);
  }
  
  public boolean canProvideEnergy(ItemStack stack) {
    return true;
  }
  
  public double getDamageAbsorptionRatio() {
    return 0.0D;
  }
  
  public int getEnergyPerDamage() {
    return 0;
  }
}
