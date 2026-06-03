package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.item.ItemIC2;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

public abstract class AbstractReactorComponent extends ItemIC2 implements IReactorComponent {
  protected AbstractReactorComponent(ItemName name) {
    super(name);
  }
  
  public void processChamber(ItemStack stack, IReactor reactor, int x, int y, boolean heatrun) {}
  
  public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
    return false;
  }
  
  public boolean canStoreHeat(ItemStack stack, IReactor reactor, int x, int y) {
    return false;
  }
  
  public int getMaxHeat(ItemStack stack, IReactor reactor, int x, int y) {
    return 0;
  }
  
  public int getCurrentHeat(ItemStack stack, IReactor reactor, int x, int y) {
    return 0;
  }
  
  public int alterHeat(ItemStack stack, IReactor reactor, int x, int y, int heat) {
    return heat;
  }
  
  public float influenceExplosion(ItemStack stack, IReactor reactor) {
    return 0.0F;
  }
  
  public boolean canBePlacedIn(ItemStack stack, IReactor reactor) {
    return true;
  }
}
