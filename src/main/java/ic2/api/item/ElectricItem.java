// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.item;

import java.util.ArrayList;
import java.util.Iterator;
import net.minecraft.item.ItemStack;
import java.util.List;

public final class ElectricItem
{
    public static IElectricItemManager manager;
    public static IElectricItemManager rawManager;
    private static final List<IBackupElectricItemManager> backupManagers;
    
    public static void registerBackupManager(final IBackupElectricItemManager manager) {
        ElectricItem.backupManagers.add(manager);
    }
    
    public static IBackupElectricItemManager getBackupManager(final ItemStack stack) {
        for (final IBackupElectricItemManager manager : ElectricItem.backupManagers) {
            if (manager.handles(stack)) {
                return manager;
            }
        }
        return null;
    }
    
    static {
        backupManagers = new ArrayList<IBackupElectricItemManager>();
    }
}
