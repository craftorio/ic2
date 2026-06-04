// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.init.Items;
import ic2.core.IC2;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropPotato extends IC2CropCard
{
    @Override
    public String getId() {
        return "potato";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(2, 0, 4, 0, 0, 2);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Yellow", "Food", "Potato" };
    }
    
    @Override
    public int getMaxSize() {
        return 4;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < 4 && crop.getLightLevel() >= 9;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 3;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() >= 3;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        if (crop.getCurrentSize() >= 4 && IC2.random.nextInt(20) <= 0) {
            return new ItemStack(Items.POISONOUS_POTATO);
        }
        if (crop.getCurrentSize() >= 3) {
            return new ItemStack(Items.POTATO);
        }
        return null;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 1;
    }
}
