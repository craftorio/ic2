// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import ic2.core.IC2;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.item.ItemStack;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.crop.IC2CropCard;

public class CropVenomilia extends IC2CropCard
{
    @Override
    public String getId() {
        return "venomilia";
    }
    
    @Override
    public String getDiscoveredBy() {
        return "raGan";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(3, 3, 1, 3, 3, 3);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Purple", "Flower", "Tulip", "Poison" };
    }
    
    @Override
    public int getMaxSize() {
        return 6;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        return (crop.getCurrentSize() <= 4 && crop.getLightLevel() >= 12) || crop.getCurrentSize() == 5;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() >= 4;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 4;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        if (crop.getCurrentSize() == 5) {
            return ItemName.crop_res.getItemStack(CropResItemType.grin_powder);
        }
        if (crop.getCurrentSize() >= 4) {
            return new ItemStack(Items.DYE, 1, 5);
        }
        return null;
    }
    
    @Override
    public int getSizeAfterHarvest(final ICropTile crop) {
        return 3;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        if (crop.getCurrentSize() >= 3) {
            return 600;
        }
        return 400;
    }
    
    @Override
    public boolean onRightClick(final ICropTile crop, final EntityPlayer player) {
        if (!player.isSneaking()) {
            this.onEntityCollision(crop, (Entity)player);
        }
        return crop.performManualHarvest();
    }
    
    @Override
    public boolean onLeftClick(final ICropTile crop, final EntityPlayer player) {
        if (!player.isSneaking()) {
            this.onEntityCollision(crop, (Entity)player);
        }
        return crop.pick();
    }
    
    @Override
    public boolean onEntityCollision(final ICropTile crop, final Entity entity) {
        if (crop.getCurrentSize() == 5 && entity instanceof EntityLivingBase) {
            if (entity instanceof EntityPlayer && ((EntityPlayer)entity).isSneaking() && IC2.random.nextInt(50) != 0) {
                return super.onEntityCollision(crop, entity);
            }
            ((EntityLivingBase)entity).addPotionEffect(new PotionEffect(MobEffects.POISON, (IC2.random.nextInt(10) + 5) * 20, 0));
            crop.setCurrentSize(4);
            crop.updateState();
        }
        return super.onEntityCollision(crop, entity);
    }
    
    @Override
    public boolean isWeed(final ICropTile crop) {
        return crop.getCurrentSize() == 5 && crop.getStatGrowth() >= 8;
    }
}
