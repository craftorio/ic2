package ic2.core.item.armor;

import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public class ItemArmorClassicCFPack extends ItemArmorUtility {
  public ItemArmorClassicCFPack() {
    super(ItemName.cf_pack, "batpack", EntityEquipmentSlot.CHEST);
    func_77656_e(260);
  }
  
  public boolean getCFPellet(EntityPlayer player, ItemStack pack) {
    if (pack.getItemDamage() > 0) {
      pack.func_77964_b(pack.getItemDamage() - 1);
      return true;
    } 
    return false;
  }
  
  public double getDurabilityForDisplay(ItemStack stack) {
    return 1.0D - super.getDurabilityForDisplay(stack);
  }
}
