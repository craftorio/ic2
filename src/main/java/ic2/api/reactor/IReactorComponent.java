package ic2.api.reactor;

import net.minecraft.item.ItemStack;

public interface IReactorComponent extends IBaseReactorComponent {
  void processChamber(ItemStack paramItemStack, IReactor paramIReactor, int paramInt1, int paramInt2, boolean paramBoolean);
  
  boolean acceptUraniumPulse(ItemStack paramItemStack1, IReactor paramIReactor, ItemStack paramItemStack2, int paramInt1, int paramInt2, int paramInt3, int paramInt4, boolean paramBoolean);
  
  boolean canStoreHeat(ItemStack paramItemStack, IReactor paramIReactor, int paramInt1, int paramInt2);
  
  int getMaxHeat(ItemStack paramItemStack, IReactor paramIReactor, int paramInt1, int paramInt2);
  
  int getCurrentHeat(ItemStack paramItemStack, IReactor paramIReactor, int paramInt1, int paramInt2);
  
  int alterHeat(ItemStack paramItemStack, IReactor paramIReactor, int paramInt1, int paramInt2, int paramInt3);
  
  float influenceExplosion(ItemStack paramItemStack, IReactor paramIReactor);
}
