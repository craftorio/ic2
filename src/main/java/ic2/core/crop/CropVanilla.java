// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import net.minecraft.block.BlockCrops;

public abstract class CropVanilla extends IC2CropCard
{
    protected final int maxAge;
    
    protected CropVanilla(final BlockCrops block) {
        this(block.getMaxAge());
    }
    
    protected CropVanilla(final int maxAge) {
        this.maxAge = maxAge;
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Notch";
    }
    
    @Override
    public int getMaxSize() {
        return this.maxAge;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() >= 9;
    }
    
    protected abstract ItemStack getSeeds();
    
    protected abstract ItemStack getProduct();
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return this.getProduct();
    }
    
    @Override
    public ItemStack getSeeds(final ICropTile crop) {
        if (crop.getStatGain() <= 1 && crop.getStatGrowth() <= 1 && crop.getStatResistance() <= 1) {
            return this.getSeeds();
        }
        return super.getSeeds(crop);
    }
}
