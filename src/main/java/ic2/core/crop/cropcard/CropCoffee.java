// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropCoffee extends IC2CropCard
{
    @Override
    public String getId() {
        return "coffee";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Snoochy";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(7, 1, 4, 1, 2, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Leaves", "Ingredient", "Beans" };
    }
    
    @Override
    public int getMaxSize() {
        return 5;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < 5 && crop.getLightLevel() >= 9;
    }
    
    @Override
    public int getWeightInfluences(final ICropTile crop, final int humidity, final int nutrients, final int air) {
        return (int)(0.4 * humidity + 1.4 * nutrients + 1.2 * air);
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() == 3) {
            return (int)(super.getGrowthDuration(crop) * 0.5);
        }
        if (crop.getCurrentSize() == 4) {
            return (int)(super.getGrowthDuration(crop) * 1.5);
        }
        return super.getGrowthDuration(crop);
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() >= 4;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        if (crop.getCurrentSize() == 4) {
            return null;
        }
        return ItemName.crop_res.getItemStack(CropResItemType.coffee_beans);
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 3;
    }
}
