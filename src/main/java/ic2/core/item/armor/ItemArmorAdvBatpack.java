package ic2.core.item.armor;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorAdvBatpack extends ItemArmorElectric {
  public ItemArmorAdvBatpack() {
    super(ItemName.advanced_batpack, "advbatpack", EntityEquipmentSlot.CHEST, 600000.0D, 1000.0D, 2);
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
