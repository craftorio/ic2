// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.entity.EntityLivingBase;

public interface IHazmatLike
{
    boolean addsProtection(final EntityLivingBase p0, final EntityEquipmentSlot p1, final ItemStack p2);
    
    default boolean fullyProtects(final EntityLivingBase entity, final EntityEquipmentSlot slot, final ItemStack stack) {
        return false;
    }
    
    default boolean hasCompleteHazmat(final EntityLivingBase living) {
        for (final EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                final ItemStack stack = living.getItemStackFromSlot(slot);
                if (stack == null || !(stack.getItem() instanceof IHazmatLike)) {
                    return false;
                }
                final IHazmatLike hazmat = (IHazmatLike)stack.getItem();
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
