package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityChargepadCESU extends TileEntityChargepadBlock {
  public TileEntityChargepadCESU() {
    super(2, 128, 300000);
  }
  
  protected void getItems(EntityPlayer player) {
    if (player != null) {
      for (ItemStack current : player.inventory.armorInventory) {
        if (current == null)
          continue; 
        chargeItem(current, 128);
      } 
      for (ItemStack current : player.inventory.mainInventory) {
        if (current == null)
          continue; 
        chargeItem(current, 128);
      } 
    } 
  }
}
