// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropWeed extends IC2CropCard
{
    @Override
    public String getId() {
        return "weed";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(0, 0, 0, 1, 0, 5);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Weed", "Bad" };
    }
    
    @Override
    public int getMaxSize() {
        return 5;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 1;
    }
    
    @Override
    public boolean onLeftClick(final ICropTile crop, final EntityPlayer player) {
        return false;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return false;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        return null;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        return 300;
    }
    
    @Override
    public boolean onEntityCollision(final ICropTile crop, final Entity entity) {
        return false;
    }
}
