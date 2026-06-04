package ic2.core.item;

import ic2.core.ref.ItemName;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBoatRubber extends EntityIC2Boat {
  public EntityBoatRubber(World world) {
    super(world);
  }
  
  protected ItemStack getItem() {
    return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.rubber);
  }
  
  protected ItemStack getBrokenItem() {
    playSound(SoundEvents.ENTITY_ITEM_PICKUP, 16.0F, 8.0F);
    return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.broken_rubber);
  }
  
  public String getTexture() {
    return "textures/models/boat_rubber.png";
  }
}
