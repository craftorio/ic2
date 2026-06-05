package ic2.api.crops;

import net.minecraft.item.ItemStack;

public interface ICropSeed {
   CropCard getCropFromStack(ItemStack var1);

   void setCropFromStack(ItemStack var1, CropCard var2);

   int getGrowthFromStack(ItemStack var1);

   void setGrowthFromStack(ItemStack var1, int var2);

   int getGainFromStack(ItemStack var1);

   void setGainFromStack(ItemStack var1, int var2);

   int getResistanceFromStack(ItemStack var1);

   void setResistanceFromStack(ItemStack var1, int var2);

   int getScannedFromStack(ItemStack var1);

   void setScannedFromStack(ItemStack var1, int var2);

   void incrementScannedFromStack(ItemStack var1);
}
