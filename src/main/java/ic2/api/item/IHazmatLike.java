package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;

public interface IHazmatLike {
  boolean addsProtection(EntityLivingBase paramEntityLivingBase, EntityEquipmentSlot paramEntityEquipmentSlot, ItemStack paramItemStack);
  
  default boolean fullyProtects(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack) {
    return false;
  }
  
  static boolean hasCompleteHazmat(EntityLivingBase living) {
    for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
      if (slot.func_188453_a() == EntityEquipmentSlot.Type.ARMOR) {
        ItemStack stack = living.func_184582_a(slot);
        if (stack == null || !(stack.func_77973_b() instanceof IHazmatLike))
          return false; 
        IHazmatLike hazmat = (IHazmatLike)stack.func_77973_b();
        if (!hazmat.addsProtection(living, slot, stack))
          return false; 
        if (hazmat.fullyProtects(living, slot, stack))
          return true; 
      } 
    } 
    return true;
  }
}
