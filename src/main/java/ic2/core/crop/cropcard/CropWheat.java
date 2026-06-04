// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.api.crops.ICropTile;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.CropProperties;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockCrops;
import ic2.core.crop.CropVanilla;

public class CropWheat extends CropVanilla
{
    public CropWheat() {
        super((BlockCrops)Blocks.WHEAT);
    }
    
    @Override
    public String getId() {
        return "wheat";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(1, 0, 4, 0, 0, 2);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Yellow", "Food", "Wheat" };
    }
    
    public ItemStack getProduct() {
        return new ItemStack(Items.WHEAT, 1);
    }
    
    public ItemStack getSeeds() {
        return new ItemStack(Items.WHEAT_SEEDS);
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 2;
    }
}
