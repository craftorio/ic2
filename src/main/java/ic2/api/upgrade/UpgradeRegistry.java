// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.upgrade;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import net.minecraft.item.ItemStack;
import java.util.List;

public class UpgradeRegistry
{
    private static final List<ItemStack> upgrades;
    
    public static ItemStack register(final ItemStack stack) {
        if (!(stack.getItem() instanceof IUpgradeItem)) {
            throw new IllegalArgumentException("The stack must represent an IUpgradeItem.");
        }
        UpgradeRegistry.upgrades.add(stack);
        return stack;
    }
    
    public static Iterable<ItemStack> getUpgrades() {
        return (Iterable<ItemStack>)Collections.unmodifiableCollection((Collection<?>)UpgradeRegistry.upgrades);
    }
    
    static {
        upgrades = new ArrayList<ItemStack>();
    }
}
