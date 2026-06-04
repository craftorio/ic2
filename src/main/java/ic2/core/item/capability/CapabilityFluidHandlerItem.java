package ic2.core.item.capability;

import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.capability.templates.FluidHandlerItemStack;

public class CapabilityFluidHandlerItem extends FluidHandlerItemStack {
  public CapabilityFluidHandlerItem(ItemStack container, int capacity) {
    super(container, capacity);
  }
  
  protected void setContainerToEmpty() {
    super.setContainerToEmpty();
    if (this.container.func_77978_p().hasNoTags())
      this.container.func_77982_d(null); 
  }
}
