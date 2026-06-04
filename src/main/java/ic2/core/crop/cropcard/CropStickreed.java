// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.entity.Entity;
import ic2.core.IC2;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropStickreed extends IC2CropCard
{
    @Override
    public String getId() {
        return "stickreed";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "raa1337";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(4, 2, 0, 1, 0, 1);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Reed", "Resin" };
    }
    
    @Override
    public int getMaxSize() {
        return 4;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < 4;
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
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 4;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        if (crop.getCurrentSize() <= 3) {
            return new ItemStack(Items.REEDS, crop.getCurrentSize() - 1);
        }
        return ItemName.misc_resource.getItemStack(MiscResourceType.resin);
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        if (crop.getCurrentSize() == 4) {
            return (byte)(3 - IC2.random.nextInt(3));
        }
        return 1;
    }
    
    @Override
    public boolean onEntityCollision(final ICropTile crop, final Entity entity) {
        return false;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() == 4) {
            return 400;
        }
        return 100;
    }
}
