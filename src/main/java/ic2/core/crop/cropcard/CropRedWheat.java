// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.util.math.BlockPos;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropRedWheat extends IC2CropCard
{
    @Override
    public String getId() {
        return "redwheat";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "raa1337";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(6, 3, 0, 0, 2, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Red", "Redstone", "Wheat" };
    }
    
    @Override
    public int getMaxSize() {
        return 7;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < 7 && crop.getLightLevel() <= 10 && crop.getLightLevel() >= 5;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() == 7;
    }
    
    @Override
    public double dropGainChance() {
        return 0.5;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 7;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        final BlockPos coords = crop.getPosition();
        if (crop.getWorldObj().isBlockIndirectlyGettingPowered(coords) > 0 || crop.getWorldObj().rand.nextBoolean()) {
            return new ItemStack(Items.REDSTONE, 1);
        }
        return new ItemStack(Items.WHEAT, 1);
    }
    
    @Override
    public boolean isRedstoneSignalEmitter(final ICropTile crop) {
        return true;
    }
    
    @Override
    public int getEmittedRedstoneSignal(final ICropTile crop) {
        return (crop.getCurrentSize() == 7) ? 15 : 0;
    }
    
    @Override
    public int getEmittedLight(final ICropTile crop) {
        return (crop.getCurrentSize() == 7) ? 7 : 0;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return 600;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 2;
    }
}
