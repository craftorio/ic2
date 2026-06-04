// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import java.util.List;
import net.minecraft.init.Items;
import ic2.core.IC2;
import java.util.ArrayList;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import net.minecraft.item.ItemStack;
import ic2.core.crop.IC2CropCard;

public class CropBaseSapling extends IC2CropCard
{
    protected final String cropName;
    protected final String saplingName;
    protected final ItemStack cropDrop;
    protected final ItemStack cropSapling;
    
    public CropBaseSapling(final String cropName, final String saplingName, final ItemStack cropDrop, final ItemStack cropSapling) {
        this.cropName = cropName;
        this.saplingName = "ic2.crop." + saplingName;
        this.cropDrop = cropDrop;
        this.cropSapling = cropSapling;
    }
    
    @Override
    public String getId() {
        return this.cropName;
    }
    
    @Override
    public String getSeedType() {
        return this.saplingName;
    }
    
    @Override
    public String getDiscoveredBy() {
        return "Speiger";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(3, 1, 0, 4, 4, 0);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Leaves", "Sapling", "Green" };
    }
    
    @Override
    public int getMaxSize() {
        return 5;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() >= 9;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() == 5;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 5;
    }
    
    @Override
    public ItemStack[] getGains(final ICropTile crop) {
        final List<ItemStack> drops = new ArrayList<ItemStack>();
        drops.add(this.cropDrop.copy());
        if (IC2.random.nextInt(100) >= 75) {
            drops.add(this.cropSapling.copy());
        }
        if (this.getId().equalsIgnoreCase("oak_sapling") && IC2.random.nextInt(100) >= 75) {
            drops.add(new ItemStack(Items.APPLE));
        }
        return drops.toArray(new ItemStack[drops.size()]);
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return (crop.getCurrentSize() >= 4) ? 150 : 600;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 4;
    }
}
