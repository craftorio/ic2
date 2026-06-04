// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;

public class CropBaseMetalUncommon extends CropBaseMetalCommon
{
    public CropBaseMetalUncommon(final String cropName, final String[] cropAttributes, final Block[] cropRootsRequirement, final ItemStack cropDrop) {
        super(cropName, cropAttributes, cropRootsRequirement, cropDrop);
    }
    
    public CropBaseMetalUncommon(final String cropName, final String[] cropAttributes, final String[] cropRootsRequirement, final ItemStack cropDrop) {
        super(cropName, cropAttributes, cropRootsRequirement, cropDrop);
    }
    
    @Override
    public int getMaxSize() {
        return 5;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        if (crop.getCurrentSize() < 4) {
            return true;
        }
        if (crop.getCurrentSize() == 4) {
            if (this.cropRootsRequirement == null || this.cropRootsRequirement.length == 0) {
                return true;
            }
            for (final Object aux : this.cropRootsRequirement) {
                if (aux instanceof String && crop.isBlockBelow((String)aux)) {
                    return true;
                }
                if (aux instanceof Block && crop.isBlockBelow((Block)aux)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(6, 2, 0, 0, 2, 0);
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() == 5;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 5;
    }
    
    @Override
    public double dropGainChance() {
        return Math.pow(0.95, this.getProperties().getTier());
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() == 4) {
            return 2200;
        }
        return 750;
    }
}
