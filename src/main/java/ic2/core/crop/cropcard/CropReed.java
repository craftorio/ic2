// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropReed extends IC2CropCard
{
    @Override
    public String getId() {
        return "reed";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Notch";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(2, 0, 0, 1, 0, 2);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Reed" };
    }
    
    @Override
    public int getMaxSize() {
        return 3;
    }
    
    @Override
    public int getWeightInfluences(final ICropTile crop, final int humidity, final int nutrients, final int air) {
        return (int)(humidity * 1.2 + nutrients + air * 0.8);
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() > 1;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return new ItemStack(Items.REEDS, crop.getCurrentSize() - 1);
    }
    
    @Override
    public boolean onEntityCollision(final ICropTile crop, final Entity entity) {
        return false;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return 200;
    }
}
