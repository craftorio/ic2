// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import ic2.api.crops.CropProperties;
import net.minecraft.init.Blocks;
import net.minecraft.block.BlockCrops;
import ic2.core.crop.CropVanilla;

public class CropBeetroot extends CropVanilla
{
    public CropBeetroot() {
        super((BlockCrops)Blocks.BEETROOTS);
    }
    
    @Override
    public String getId() {
        return "beetroots";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(1, 0, 4, 0, 1, 2);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Red", "Food", "Beetroot" };
    }
    
    public ItemStack getProduct() {
        return new ItemStack(Items.BEETROOT, 1);
    }
    
    public ItemStack getSeeds() {
        return new ItemStack(Items.BEETROOT_SEEDS);
    }
}
