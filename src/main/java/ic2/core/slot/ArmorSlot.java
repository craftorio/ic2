// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
// TODO(Ravel): Failed to fully resolve file: null cannot be cast to non-null type com.intellij.psi.PsiJavaCodeReferenceElement
package ic2.core.slot;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import net.minecraft.inventory.EntityEquipmentSlot;

public class ArmorSlot {
  public static EntityEquipmentSlot get(int index) {
    return armorSlots[index];
  }
  
  public static int getCount() {
    return armorSlots.length;
  }
  
  public static Iterable<EntityEquipmentSlot> getAll() {
    return armorSlotList;
  }
  
  private static EntityEquipmentSlot[] getArmorSlots() {
    EntityEquipmentSlot[] values = EntityEquipmentSlot.values();
    int count = 0;
    for (EntityEquipmentSlot slot : values) {
      if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR)
        count++; 
    } 
    EntityEquipmentSlot[] ret = new EntityEquipmentSlot[count];
    int i;
    for (i = 0; i < ret.length; i++) {
      for (EntityEquipmentSlot slot : values) {
        if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR && slot.func_188454_b() == i) {
          ret[i] = slot;
          break;
        } 
      } 
    } 
    for (i = 0; i < ret.length; i++) {
      if (ret[i] == null)
        throw new RuntimeException("Can't find an armor mapping for idx " + i); 
    } 
    return ret;
  }
  
  private static final EntityEquipmentSlot[] armorSlots = getArmorSlots();
  
  private static final List<EntityEquipmentSlot> armorSlotList = Collections.unmodifiableList(Arrays.asList(armorSlots));
}
