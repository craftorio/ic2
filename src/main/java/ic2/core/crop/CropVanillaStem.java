// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import ic2.api.crops.ICropTile;

public abstract class CropVanillaStem extends CropVanilla
{
    protected CropVanillaStem(final int maxAge) {
        super(maxAge);
    }
    
    @Override
    public int getWeightInfluences(final ICropTile crop, final int humidity, final int nutrients, final int air) {
        return (int)(humidity * 1.1 + nutrients * 0.9 + air);
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return this.maxAge - 1;
    }
}
