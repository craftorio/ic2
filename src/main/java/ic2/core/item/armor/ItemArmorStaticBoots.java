package ic2.core.item.armor;

import ic2.api.item.ElectricItem;
import ic2.core.ref.ItemName;
import ic2.core.util.StackUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ItemArmorStaticBoots extends ItemArmorUtility {
  public ItemArmorStaticBoots() {
    super(ItemName.static_boots, "rubber", EntityEquipmentSlot.FEET);
  }
  
  public void onArmorTick(World world, EntityPlayer player, ItemStack stack) {
    if (StackUtil.isEmpty((ItemStack)player.inventory.field_70460_b.get(2)))
      return; 
    boolean ret = false;
    NBTTagCompound compound = StackUtil.getOrCreateNbtData(stack);
    boolean isNotWalking = (player.func_184187_bx() != null || player.func_70090_H());
    if (!compound.func_74764_b("x") || isNotWalking)
      compound.func_74768_a("x", (int)player.posX); 
    if (!compound.func_74764_b("z") || isNotWalking)
      compound.func_74768_a("z", (int)player.posZ); 
    double distance = Math.sqrt(((compound.func_74762_e("x") - (int)player.posX) * (compound.func_74762_e("x") - (int)player.posX) + (compound.func_74762_e("z") - (int)player.posZ) * (compound.func_74762_e("z") - (int)player.posZ)));
    if (distance >= 5.0D) {
      compound.func_74768_a("x", (int)player.posX);
      compound.func_74768_a("z", (int)player.posZ);
      ret = (ElectricItem.manager.charge((ItemStack)player.inventory.field_70460_b.get(2), Math.min(3.0D, distance / 5.0D), 2147483647, true, false) > 0.0D);
    } 
    if (ret)
      player.field_71069_bz.func_75142_b(); 
  }
}
