package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public class ItemReactorCondensator extends AbstractDamageableReactorComponent {
  public ItemReactorCondensator(ItemName name, int maxdmg) {
    super(name, maxdmg);
  }
  
  public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y) {
    return (getCurrentHeat(stack) < getMaxCustomDamage(stack));
  }
  
  public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y) {
    return getMaxCustomDamage(stack);
  }
  
  private int getCurrentHeat(ItemStack stack) {
    return getCustomDamage(stack);
  }
  
  public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat) {
    if (heat < 0)
      return heat; 
    int currentHeat = getCurrentHeat(stack);
    int amount = Math.min(heat, getMaxHeat(stack, reactor, x, y) - currentHeat);
    heat -= amount;
    setCustomDamage(stack, currentHeat + amount);
    return heat;
  }
}
