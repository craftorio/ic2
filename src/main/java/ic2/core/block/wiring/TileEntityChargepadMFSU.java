package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityChargepadMFSU extends TileEntityChargepadBlock {
  public TileEntityChargepadMFSU() {
    super(4, 2048, 40000000);
  }
  
  protected void getItems(EntityPlayer player) {
    for (ItemStack current : player.inventory.field_70460_b) {
      if (current == null)
        continue; 
      chargeItem(current, 2048);
    } 
    for (ItemStack current : player.inventory.field_70462_a) {
      if (current == null)
        continue; 
      chargeItem(current, 2048);
    } 
  }
}
