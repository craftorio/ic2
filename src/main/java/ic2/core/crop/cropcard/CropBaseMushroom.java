// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import net.minecraft.item.ItemStack;
import ic2.core.crop.IC2CropCard;

public class CropBaseMushroom extends IC2CropCard
{
    protected final String cropId;
    protected final String[] cropAttributes;
    protected final ItemStack cropDrop;
    
    public CropBaseMushroom(final String cropId, final String[] cropAttributes, final ItemStack cropDrop) {
        this.cropId = cropId;
        this.cropAttributes = cropAttributes;
        this.cropDrop = cropDrop;
    }
    
    @Override
    public String getId() {
        return this.cropId;
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(2, 0, 4, 0, 0, 4);
    }
    
    @Override
    public String[] getAttributes() {
        return this.cropAttributes;
    }
    
    @Override
    public int getMaxSize() {
        return 3;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < this.getMaxSize() && crop.getStorageWater() > 0;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return this.cropDrop.copy();
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return 200;
    }
}
