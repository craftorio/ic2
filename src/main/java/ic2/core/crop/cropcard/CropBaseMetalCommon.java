// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import ic2.core.crop.IC2CropCard;

public class CropBaseMetalCommon extends IC2CropCard
{
    protected final String cropName;
    protected final String[] cropAttributes;
    protected final Object[] cropRootsRequirement;
    protected final ItemStack cropDrop;
    
    public CropBaseMetalCommon(final String cropName, final String[] cropAttributes, final Block[] cropRootsRequirement, final ItemStack cropDrop) {
        this.cropName = cropName;
        this.cropAttributes = cropAttributes;
        this.cropRootsRequirement = cropRootsRequirement;
        this.cropDrop = cropDrop;
    }
    
    public CropBaseMetalCommon(final String cropName, final String[] cropAttributes, final String[] cropRootsRequirement, final ItemStack cropDrop) {
        this.cropName = cropName;
        this.cropAttributes = cropAttributes;
        this.cropRootsRequirement = cropRootsRequirement;
        this.cropDrop = cropDrop;
    }
    
    @Override
    public String getId() {
        return this.cropName;
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(6, 2, 0, 0, 1, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return this.cropAttributes;
    }
    
    @Override
    public int getMaxSize() {
        return 4;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        if (crop.getCurrentSize() < 3) {
            return true;
        }
        if (crop.getCurrentSize() == 3) {
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
    public int getRootsLength(final ICropTile crop) {
        return 5;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() == 4;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 4;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return this.cropDrop.copy();
    }
    
    @Override
    public double dropGainChance() {
        return super.dropGainChance() / 2.0;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return (crop.getCurrentSize() == 3) ? 2000 : 800;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 2;
    }
}
