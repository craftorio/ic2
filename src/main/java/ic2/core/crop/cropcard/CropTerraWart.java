// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.core.crop.IC2Crops;
import net.minecraft.init.Blocks;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropTerraWart extends IC2CropCard
{
    @Override
    public String getId() {
        return "terra_wart";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(5, 2, 4, 0, 3, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Blue", "Aether", "Consumable", "Snow" };
    }
    
    @Override
    public int getMaxSize() {
        return 3;
    }
    
    @Override
    public double dropGainChance() {
        return 0.8;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return ItemName.terra_wart.getItemStack();
    }
    
    @Override
    public void tick(final ICropTile crop) {
        if (crop.isBlockBelow(Blocks.SNOW)) {
            if (this.canGrow(crop)) {
                crop.setGrowthPoints(crop.getGrowthPoints() + 100);
            }
        }
        else if (crop.isBlockBelow(Blocks.SOUL_SAND) && crop.getWorldObj().rand.nextInt(300) == 0) {
            crop.setCrop(IC2Crops.cropNetherWart);
        }
    }
    
    @Override
    public int getRootsLength(final ICropTile crop) {
        return 5;
    }
}
