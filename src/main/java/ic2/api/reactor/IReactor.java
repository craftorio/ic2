package ic2.api.reactor;

import ic2.api.info.ILocatable;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

public interface IReactor extends ILocatable {
  TileEntity getCoreTe();
  
  int getHeat();
  
  void setHeat(int paramInt);
  
  int addHeat(int paramInt);
  
  int getMaxHeat();
  
  void setMaxHeat(int paramInt);
  
  void addEmitHeat(int paramInt);
  
  float getHeatEffectModifier();
  
  void setHeatEffectModifier(float paramFloat);
  
  float getReactorEnergyOutput();
  
  double getReactorEUEnergyOutput();
  
  float addOutput(float paramFloat);
  
  ItemStack getItemAt(int paramInt1, int paramInt2);
  
  void setItemAt(int paramInt1, int paramInt2, ItemStack paramItemStack);
  
  void explode();
  
  int getTickRate();
  
  boolean produceEnergy();
  
  boolean isFluidCooled();
}
