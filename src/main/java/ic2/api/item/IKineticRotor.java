package ic2.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public interface IKineticRotor {
  int getDiameter(ItemStack paramItemStack);
  
  ResourceLocation getRotorRenderTexture(ItemStack paramItemStack);
  
  float getEfficiency(ItemStack paramItemStack);
  
  int getMinWindStrength(ItemStack paramItemStack);
  
  int getMaxWindStrength(ItemStack paramItemStack);
  
  boolean isAcceptedType(ItemStack paramItemStack, GearboxType paramGearboxType);
  
  public enum GearboxType {
    WATER, WIND;
  }
}
