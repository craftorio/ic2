package ic2.api.item;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.EntityEquipmentSlot.Type;
import net.minecraft.item.ItemStack;

public interface IHazmatLike {
   boolean addsProtection(EntityLivingBase var1, EntityEquipmentSlot var2, ItemStack var3);

   default boolean fullyProtects(EntityLivingBase entity, EntityEquipmentSlot slot, ItemStack stack) {
      return false;
   }

   static boolean hasCompleteHazmat(EntityLivingBase living) {
      for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
         if (slot.getSlotType() == Type.ARMOR) {
            ItemStack stack = living.getItemStackFromSlot(slot);
            if (stack == null || !(stack.getItem() instanceof IHazmatLike)) {
               return false;
            }

            IHazmatLike hazmat = (IHazmatLike)stack.getItem();
            if (!hazmat.addsProtection(living, slot, stack)) {
               return false;
            }

            if (hazmat.fullyProtects(living, slot, stack)) {
               return true;
            }
         }
      }

      return true;
   }
}
