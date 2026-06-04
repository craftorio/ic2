// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropColorFlower extends IC2CropCard
{
    public String name;
    public String[] attributes;
    public int color;
    
    public CropColorFlower(final String n, final String[] a, final int c) {
        this.name = n;
        this.attributes = a;
        this.color = c;
    }
    
    @Override
    public String getDiscoveredBy() {
        if (this.name.equals("dandelion") || this.name.equals("rose")) {
            return "Notch";
        }
        return "Alblaka";
    }
    
    @Override
    public String getId() {
        return this.name;
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(2, 1, 1, 0, 5, 1);
    }
    
    @Override
    public String[] getAttributes() {
        return this.attributes;
    }
    
    @Override
    public int getMaxSize() {
        return 4;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() <= 3 && crop.getLightLevel() >= 12;
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
        return new ItemStack(Items.DYE, 1, this.color);
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 3;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() == 3) {
            return 600;
        }
        return 400;
    }
}
