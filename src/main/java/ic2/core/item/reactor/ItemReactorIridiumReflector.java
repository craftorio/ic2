package ic2.core.item.reactor;

import ic2.api.reactor.IReactor;
import ic2.api.reactor.IReactorComponent;
import ic2.core.profile.NotClassic;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;

@NotClassic
public class ItemReactorIridiumReflector extends AbstractReactorComponent {
  public ItemReactorIridiumReflector(ItemName name) {
    super(name);
  }
  
  public boolean acceptUraniumPulse(ItemStack stack, IReactor reactor, ItemStack pulsingStack, int youX, int youY, int pulseX, int pulseY, boolean heatrun) {
    if (!heatrun) {
      IReactorComponent source = (IReactorComponent)pulsingStack.func_77973_b();
      source.acceptUraniumPulse(pulsingStack, reactor, stack, pulseX, pulseY, youX, youY, heatrun);
    } 
    return true;
  }
  
  public float influenceExplosion(ItemStack stack, IReactor reactor) {
    return -1.0F;
  }
}
