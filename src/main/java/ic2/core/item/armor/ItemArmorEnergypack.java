package ic2.core.item.armor;

import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemArmorEnergypack extends ItemArmorElectric {
  public ItemArmorEnergypack() {
    super(ItemName.energy_pack, "energypack", EntityEquipmentSlot.CHEST, 2000000.0D, 1000.0D, 3);
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
