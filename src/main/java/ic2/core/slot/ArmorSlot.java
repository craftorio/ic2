// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.slot;

import java.util.Collections;
import java.util.Arrays;
import java.util.List;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ArmorSlot
{
    private static final EntityEquipmentSlot[] armorSlots;
    private static final List<EntityEquipmentSlot> armorSlotList;
    
    public static EntityEquipmentSlot get(final int index) {
        return ArmorSlot.armorSlots[index];
    }
    
    public static int getCount() {
        return ArmorSlot.armorSlots.length;
    }
    
    public static Iterable<EntityEquipmentSlot> getAll() {
        return ArmorSlot.armorSlotList;
    }
    
    private static EntityEquipmentSlot[] getArmorSlots() {
        final EntityEquipmentSlot[] values = EntityEquipmentSlot.values();
        int count = 0;
        for (final EntityEquipmentSlot slot : values) {
            if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                ++count;
            }
        }
        final EntityEquipmentSlot[] ret = new EntityEquipmentSlot[count];
        for (int i = 0; i < ret.length; ++i) {
            for (final EntityEquipmentSlot slot2 : values) {
                if (slot2.getSlotType() == EntityEquipmentSlot.Type.ARMOR && slot2.getIndex() == i) {
                    ret[i] = slot2;
                    break;
                }
            }
        }
        for (int i = 0; i < ret.length; ++i) {
            if (ret[i] == null) {
                throw new RuntimeException("Can't find an armor mapping for idx " + i);
            }
        }
        return ret;
    }
    
    static {
        armorSlots = getArmorSlots();
        armorSlotList = Collections.unmodifiableList((List<? extends EntityEquipmentSlot>)Arrays.asList((T[])ArmorSlot.armorSlots));
    }
}
