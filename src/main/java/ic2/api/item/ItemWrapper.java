package ic2.api.item;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class ItemWrapper {
  private static final Multimap<Item, IBoxable> boxableItems = (Multimap<Item, IBoxable>)ArrayListMultimap.create();
  
  private static final Multimap<Item, IMetalArmor> metalArmorItems = (Multimap<Item, IMetalArmor>)ArrayListMultimap.create();
  
  public static void registerBoxable(Item item, IBoxable boxable) {
    boxableItems.put(item, boxable);
  }
  
  public static boolean canBeStoredInToolbox(ItemStack stack) {
    Item item = stack.func_77973_b();
    for (IBoxable boxable : boxableItems.get(item)) {
      if (boxable.canBeStoredInToolbox(stack))
        return true; 
    } 
    if (item instanceof IBoxable && ((IBoxable)item).canBeStoredInToolbox(stack))
      return true; 
    return false;
  }
  
  public static void registerMetalArmor(Item item, IMetalArmor armor) {
    metalArmorItems.put(item, armor);
  }
  
  public static boolean isMetalArmor(ItemStack stack, EntityPlayer player) {
    Item item = stack.func_77973_b();
    for (IMetalArmor metalArmor : metalArmorItems.get(item)) {
      if (metalArmor.isMetalArmor(stack, player))
        return true; 
    } 
    if (item instanceof IMetalArmor && ((IMetalArmor)item).isMetalArmor(stack, player))
      return true; 
    return false;
  }
}
