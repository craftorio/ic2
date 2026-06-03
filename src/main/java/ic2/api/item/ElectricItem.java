package ic2.api.item;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.item.ItemStack;

public final class ElectricItem {
  public static IElectricItemManager manager;
  
  public static IElectricItemManager rawManager;
  
  public static void registerBackupManager(IBackupElectricItemManager manager) {
    backupManagers.add(manager);
  }
  
  public static IBackupElectricItemManager getBackupManager(ItemStack stack) {
    for (IBackupElectricItemManager manager : backupManagers) {
      if (manager.handles(stack))
        return manager; 
    } 
    return null;
  }
  
  private static final List<IBackupElectricItemManager> backupManagers = new ArrayList<>();
}
