package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.block.generator.tileentity.TileEntitySolarGenerator;
import ic2.core.ref.ItemName;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemArmorSolarHelmet extends ItemArmorUtility {
  public ItemArmorSolarHelmet() {
    super(ItemName.solar_helmet, "solar", EntityEquipmentSlot.HEAD);
    setMaxDamage(0);
  }
  
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    boolean ret = false;
    if (player.inventory.armorInventory.get(2) != null) {
      double chargeAmount = TileEntitySolarGenerator.getSkyLight(player.getEntityWorld(), player.getPosition());
      if (chargeAmount > 0.0D)
        ret = (ElectricItem.manager.charge((ItemStack)player.inventory.armorInventory.get(2), chargeAmount, 2147483647, true, false) > 0.0D); 
    } 
    if (ret)
      player.inventoryContainer.detectAndSendChanges(); 
  }
  
  public int getItemEnchantability() {
    return 0;
  }
}
