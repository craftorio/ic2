package ic2.api.crops;

import ic2.api.info.ILocatable;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICropTile extends ILocatable {
  CropCard getCrop();
  
  void setCrop(CropCard paramCropCard);
  
  int getCurrentSize();
  
  void setCurrentSize(int paramInt);
  
  int getStatGrowth();
  
  void setStatGrowth(int paramInt);
  
  int getStatGain();
  
  void setStatGain(int paramInt);
  
  int getStatResistance();
  
  void setStatResistance(int paramInt);
  
  int getStorageNutrients();
  
  void setStorageNutrients(int paramInt);
  
  int getStorageWater();
  
  void setStorageWater(int paramInt);
  
  int getStorageWeedEX();
  
  void setStorageWeedEX(int paramInt);
  
  int getScanLevel();
  
  void setScanLevel(int paramInt);
  
  int getGrowthPoints();
  
  void setGrowthPoints(int paramInt);
  
  boolean isCrossingBase();
  
  void setCrossingBase(boolean paramBoolean);
  
  NBTTagCompound getCustomData();
  
  int getTerrainHumidity();
  
  int getTerrainNutrients();
  
  int getTerrainAirQuality();
  
  @Deprecated
  World getWorld();
  
  @Deprecated
  BlockPos getLocation();
  
  int getLightLevel();
  
  boolean pick();
  
  boolean performManualHarvest();
  
  List<ItemStack> performHarvest();
  
  void reset();
  
  void updateState();
  
  boolean isBlockBelow(Block paramBlock);
  
  boolean isBlockBelow(String paramString);
  
  ItemStack generateSeeds(CropCard paramCropCard, int paramInt1, int paramInt2, int paramInt3, int paramInt4);
}
