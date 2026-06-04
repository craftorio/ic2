// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

public class BaseSeed
{
    public final CropCard crop;
    public int size;
    public int statGrowth;
    public int statGain;
    public int statResistance;
    
    public BaseSeed(final CropCard crop, final int size, final int statGrowth, final int statGain, final int statResistance) {
        this.crop = crop;
        this.size = size;
        this.statGrowth = statGrowth;
        this.statGain = statGain;
        this.statResistance = statResistance;
    }
    
    @Deprecated
    public BaseSeed(final CropCard crop, final int size, final int statGrowth, final int statGain, final int statResistance, final int stackSize) {
        this(crop, size, statGrowth, statGain, statResistance);
    }
}
