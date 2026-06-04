package ic2.core.item;

import ic2.api.item.ElectricItem;
import ic2.core.IC2;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class EntityBoatElectric extends EntityIC2Boat {
  private static final double euConsume = 4.0D;
  
  private boolean accelerated;
  
  public EntityBoatElectric(World world) {
    super(world);
    this.accelerated = false;
    this.isImmuneToFire = true;
  }
  
  protected ItemStack getItem() {
    return ItemName.boat.getItemStack(ItemIC2Boat.BoatType.electric);
  }
  
  protected double getAccelerationFactor() {
    return this.accelerated ? 1.5D : 0.25D;
  }
  
  protected double getTopSpeed() {
    return 0.7D;
  }
  
  protected boolean isWater(IBlockState block) {
    return (block.getMaterial() == Material.WATER || block.getMaterial() == Material.LAVA);
  }
  
  public String getTexture() {
    return "textures/models/boat_electric.png";
  }
  
  public void onUpdate() {
    extinguish();
    for (Entity e : getRecursivePassengers())
      e.extinguish(); 
    this.accelerated = false;
    Entity driver = getControllingPassenger();
    if (driver instanceof EntityPlayer && IC2.keyboard.isForwardKeyDown((EntityPlayer)driver))
      for (ItemStack stack : ((EntityPlayer)driver).inventory.armorInventory) {
        if (!StackUtil.isEmpty(stack) && ElectricItem.manager.discharge(stack, 4.0D, 2147483647, true, true, true) == 4.0D) {
          ElectricItem.manager.discharge(stack, 4.0D, 2147483647, true, true, false);
          this.accelerated = true;
          break;
        } 
      }  
    super.onUpdate();
  }
}
