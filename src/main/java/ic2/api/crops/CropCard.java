// 
// Decompiled by Procyon v0.6.0
// 

package ic2.api.crops;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import net.minecraft.world.World;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.EntityPlayer;

public abstract class CropCard
{
    public abstract String getId();
    
    public abstract String getOwner();
    
    public String getUnlocalizedName() {
        return this.getOwner() + ".crop." + this.getId();
    }
    
    public String getDiscoveredBy() {
        return "unknown";
    }
    
    public String desc(final int i) {
        final String[] att = this.getAttributes();
        if (att == null || att.length == 0) {
            return "";
        }
        if (i == 0) {
            String s = att[0];
            if (att.length >= 2) {
                s = s + ", " + att[1];
                if (att.length >= 3) {
                    s += ",";
                }
            }
            return s;
        }
        if (att.length < 3) {
            return "";
        }
        String s = att[2];
        if (att.length >= 4) {
            s = s + ", " + att[3];
        }
        return s;
    }
    
    public int getRootsLength(final ICropTile cropTile) {
        return 1;
    }
    
    public abstract CropProperties getProperties();
    
    public String[] getAttributes() {
        return new String[0];
    }
    
    public String getSeedType() {
        return "ic2.crop.seeds";
    }
    
    public abstract int getMaxSize();
    
    public int getGrowthDuration(final ICropTile cropTile) {
        return this.getProperties().getTier() * 200;
    }
    
    public boolean canGrow(final ICropTile cropTile) {
        return cropTile.getCurrentSize() < this.getMaxSize();
    }
    
    public int getWeightInfluences(final ICropTile crop, final int humidity, final int nutrients, final int air) {
        return humidity + nutrients + air;
    }
    
    public boolean canCross(final ICropTile crop) {
        return crop.getCurrentSize() >= 3;
    }
    
    public boolean onRightClick(final ICropTile cropTile, final EntityPlayer player) {
        return cropTile.performManualHarvest();
    }
    
    public int getOptimalHarvestSize(final ICropTile cropTile) {
        return this.getMaxSize();
    }
    
    public boolean canBeHarvested(final ICropTile cropTile) {
        return cropTile.getCurrentSize() == this.getMaxSize();
    }
    
    public double dropGainChance() {
        return Math.pow(0.95, this.getProperties().getTier());
    }
    
    @Deprecated
    public ItemStack getGain(final ICropTile crop) {
        return ItemStack.EMPTY;
    }
    
    public ItemStack[] getGains(final ICropTile crop) {
        return new ItemStack[] { this.getGain(crop) };
    }
    
    public int getSizeAfterHarvest(final ICropTile cropTile) {
        return 1;
    }
    
    public boolean onLeftClick(final ICropTile cropTile, final EntityPlayer player) {
        return cropTile.pick();
    }
    
    public float dropSeedChance(final ICropTile crop) {
        if (crop.getCurrentSize() == 1) {
            return 0.0f;
        }
        float base = 0.5f;
        if (crop.getCurrentSize() == 2) {
            base /= 2.0f;
        }
        for (int i = 0; i < this.getProperties().getTier(); ++i) {
            base *= (float)0.8;
        }
        return base;
    }
    
    public ItemStack getSeeds(final ICropTile crop) {
        return crop.generateSeeds(crop.getCrop(), crop.getStatGrowth(), crop.getStatGain(), crop.getStatResistance(), crop.getScanLevel());
    }
    
    public void onNeighbourChange(final ICropTile crop) {
    }
    
    public boolean isRedstoneSignalEmitter(final ICropTile cropTile) {
        return false;
    }
    
    public int getEmittedRedstoneSignal(final ICropTile cropTile) {
        return 0;
    }
    
    public void onBlockDestroyed(final ICropTile crop) {
    }
    
    public int getEmittedLight(final ICropTile crop) {
        return 0;
    }
    
    public boolean onEntityCollision(final ICropTile crop, final Entity entity) {
        return entity instanceof EntityLivingBase && entity.isSprinting();
    }
    
    public void tick(final ICropTile cropTile) {
    }
    
    public boolean isWeed(final ICropTile cropTile) {
        return cropTile.getCurrentSize() >= 2 && (cropTile.getCrop() == Crops.weed || cropTile.getStatGrowth() >= 24);
    }
    
    public World getWorld(final ICropTile cropTile) {
        return cropTile.getWorldObj();
    }
    
    @SideOnly(Side.CLIENT)
    public abstract List<ResourceLocation> getTexturesLocation();
}
