// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import com.google.common.collect.ArrayListMultimap;
import net.minecraft.entity.player.EntityPlayer;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Item;
import com.google.common.collect.Multimap;

public class ItemWrapper
{
    private static final Multimap<Item, IBoxable> boxableItems;
    private static final Multimap<Item, IMetalArmor> metalArmorItems;
    
    public static void registerBoxable(final Item item, final IBoxable boxable) {
        ItemWrapper.boxableItems.put((Object)item, (Object)boxable);
    }
    
    public static boolean canBeStoredInToolbox(final ItemStack stack) {
        final Item item = stack.getItem();
        for (final IBoxable boxable : ItemWrapper.boxableItems.get((Object)item)) {
            if (boxable.canBeStoredInToolbox(stack)) {
                return true;
            }
        }
        return item instanceof IBoxable && ((IBoxable)item).canBeStoredInToolbox(stack);
    }
    
    public static void registerMetalArmor(final Item item, final IMetalArmor armor) {
        ItemWrapper.metalArmorItems.put((Object)item, (Object)armor);
    }
    
    public static boolean isMetalArmor(final ItemStack stack, final EntityPlayer player) {
        final Item item = stack.getItem();
        for (final IMetalArmor metalArmor : ItemWrapper.metalArmorItems.get((Object)item)) {
            if (metalArmor.isMetalArmor(stack, player)) {
                return true;
            }
        }
        return item instanceof IMetalArmor && ((IMetalArmor)item).isMetalArmor(stack, player);
    }
    
    static {
        boxableItems = (Multimap)ArrayListMultimap.create();
        metalArmorItems = (Multimap)ArrayListMultimap.create();
    }
}
