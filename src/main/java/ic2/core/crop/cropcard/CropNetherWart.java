// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.core.crop.IC2Crops;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropNetherWart extends IC2CropCard
{
    @Override
    public String getId() {
        return "nether_wart";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Notch";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(5, 4, 2, 0, 2, 1);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Red", "Nether", "Ingredient", "Soulsand" };
    }
    
    @Override
    public int getMaxSize() {
        return 3;
    }
    
    @Override
    public double dropGainChance() {
        return 2.0;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return new ItemStack(Items.NETHER_WART, 1);
    }
    
    @Override
    public void tick(final ICropTile crop) {
        if (crop.isBlockBelow(Blocks.SOUL_SAND)) {
            if (this.canGrow(crop)) {
                crop.setGrowthPoints(crop.getGrowthPoints() + 100);
            }
        }
        else if (crop.isBlockBelow(Blocks.SNOW) && crop.getWorldObj().rand.nextInt(300) == 0) {
            crop.setCrop(IC2Crops.cropTerraWart);
        }
    }
    
    @Override
    public int getRootsLength(final ICropTile crop) {
        return 5;
    }
}
