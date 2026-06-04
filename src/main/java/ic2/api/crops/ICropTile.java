// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import java.util.List;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.nbt.NBTTagCompound;
import ic2.api.info.ILocatable;

public interface ICropTile extends ILocatable
{
    CropCard getCrop();
    
    void setCrop(final CropCard p0);
    
    int getCurrentSize();
    
    void setCurrentSize(final int p0);
    
    int getStatGrowth();
    
    void setStatGrowth(final int p0);
    
    int getStatGain();
    
    void setStatGain(final int p0);
    
    int getStatResistance();
    
    void setStatResistance(final int p0);
    
    int getStorageNutrients();
    
    void setStorageNutrients(final int p0);
    
    int getStorageWater();
    
    void setStorageWater(final int p0);
    
    int getStorageWeedEX();
    
    void setStorageWeedEX(final int p0);
    
    int getScanLevel();
    
    void setScanLevel(final int p0);
    
    int getGrowthPoints();
    
    void setGrowthPoints(final int p0);
    
    boolean isCrossingBase();
    
    void setCrossingBase(final boolean p0);
    
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
    
    boolean isBlockBelow(final Block p0);
    
    boolean isBlockBelow(final String p0);
    
    ItemStack generateSeeds(final CropCard p0, final int p1, final int p2, final int p3, final int p4);
}
