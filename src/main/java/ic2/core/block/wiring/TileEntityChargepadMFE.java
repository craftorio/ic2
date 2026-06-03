package ic2.core.block.wiring;

import ic2.core.profile.NotClassic;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

@NotClassic
public class TileEntityChargepadMFE extends TileEntityChargepadBlock {
  public TileEntityChargepadMFE() {
    super(3, 512, 4000000);
  }
  
  protected void getItems(EntityPlayer player) {
    if (player != null) {
      for (ItemStack current : player.field_71071_by.field_70460_b) {
        if (current == null)
          continue; 
        chargeItem(current, 512);
      } 
      for (ItemStack current : player.field_71071_by.field_70462_a) {
        if (current == null)
          continue; 
        chargeItem(current, 512);
      } 
    } 
  }
}
