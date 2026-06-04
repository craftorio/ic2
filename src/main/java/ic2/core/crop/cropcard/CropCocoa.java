// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropCocoa extends IC2CropCard
{
    @Override
    public String getId() {
        return "cocoa";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Notch";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(3, 1, 3, 0, 4, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Brown", "Food", "Stem" };
    }
    
    @Override
    public int getMaxSize() {
        return 4;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() <= 3 && crop.getStorageNutrients() >= 3;
    }
    
    @Override
    public int getWeightInfluences(final ICropTile crop, final int humidity, final int nutrients, final int air) {
        return (int)(humidity * 0.8 + nutrients * 1.3 + air * 0.9);
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 4;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() == 4;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return new ItemStack(Items.DYE, 1, 3);
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() == 3) {
            return 900;
        }
        return 400;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 3;
    }
}
