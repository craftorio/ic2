package ic2.api.crops;

import ic2.api.info.ILocatable;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ICropTile extends ILocatable
{
	CropCard getCrop();

	void setCrop(CropCard var1);

	int getCurrentSize();

	void setCurrentSize(int var1);

	int getStatGrowth();

	void setStatGrowth(int var1);

	int getStatGain();

	void setStatGain(int var1);

	int getStatResistance();

	void setStatResistance(int var1);

	int getStorageNutrients();

	void setStorageNutrients(int var1);

	int getStorageWater();

	void setStorageWater(int var1);

	int getStorageWeedEX();

	void setStorageWeedEX(int var1);

	int getScanLevel();

	void setScanLevel(int var1);

	int getGrowthPoints();

	void setGrowthPoints(int var1);

	boolean isCrossingBase();

	void setCrossingBase(boolean var1);

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

	boolean isBlockBelow(Block var1);

	boolean isBlockBelow(String var1);

	ItemStack generateSeeds(CropCard var1, int var2, int var3, int var4, int var5);
}
