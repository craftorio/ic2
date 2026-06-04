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
      for (ItemStack current : player.inventory.field_70460_b) {
        if (current == null)
          continue; 
        chargeItem(current, 128);
      } 
      for (ItemStack current : player.inventory.field_70462_a) {
        if (current == null)
          continue; 
        chargeItem(current, 128);
      } 
    } 
  }
}
