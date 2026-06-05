package ic2.api.reactor;

import net.minecraft.item.ItemStack;

public interface IBaseReactorComponent {
   boolean canBePlacedIn(ItemStack var1, IReactor var2);
}
