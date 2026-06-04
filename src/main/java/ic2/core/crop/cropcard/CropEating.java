// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop.cropcard;

import ic2.api.item.ItemWrapper;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import ic2.core.util.BiomeUtil;
import java.util.Iterator;
import net.minecraft.util.math.BlockPos;
import ic2.core.IC2;
import net.minecraft.util.SoundCategory;
import net.minecraft.init.SoundEvents;
import net.minecraft.potion.PotionEffect;
import net.minecraft.init.MobEffects;
import net.minecraft.util.DamageSource;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;
import java.util.Collections;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.entity.EntityLivingBase;
import ic2.core.util.StackUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import ic2.api.crops.ICropTile;
import ic2.api.crops.CropProperties;
import ic2.core.IC2DamageSource;
import ic2.core.crop.IC2CropCard;

public class CropEating extends IC2CropCard
{
    private final double movementMultiplier = 0.5;
    private final double length = 1.0;
    private static final IC2DamageSource damage;
    
    @Override
    public String getDiscoveredBy() {
        return "Hasudako";
    }
    
    @Override
    public String getId() {
        return "eatingplant";
    }
    
    @Override
    public CropProperties getProperties() {
        return new CropProperties(6, 1, 1, 3, 1, 4);
    }
    
    @Override
    public String[] getAttributes() {
        return new String[] { "Bad", "Food" };
    }
    
    @Override
    public int getMaxSize() {
        return 6;
    }
    
    @Override
    public boolean canGrow(final ICropTile crop) {
        if (crop.getCurrentSize() < 3) {
            return crop.getLightLevel() > 10;
        }
        return crop.isBlockBelow((Block)Blocks.LAVA) && crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() > 10;
    }
    
    @Override
    public int getOptimalHarvestSize(final ICropTile crop) {
        return 4;
    }
    
    @Override
    public boolean canBeHarvested(final ICropTile crop) {
        return crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6;
    }
    
    @Override
    public ItemStack getGain(final ICropTile crop) {
        if (crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6) {
            return new ItemStack((Block)Blocks.CACTUS);
        }
        return null;
    }
    
    @Override
    public void tick(final ICropTile crop) {
        if (crop.getCurrentSize() == 1) {
            return;
        }
        final BlockPos coords = crop.getPosition();
        final double xcentered = coords.getX() + 0.5;
        final double ycentered = coords.getY() + 0.5;
        final double zcentered = coords.getZ() + 0.5;
        if (crop.getCustomData().getBoolean("eaten")) {
            StackUtil.dropAsEntity(crop.getWorldObj(), coords, new ItemStack(Items.ROTTEN_FLESH));
            crop.getCustomData().setBoolean("eaten", false);
        }
        final List<EntityLivingBase> list = crop.getWorldObj().getEntitiesWithinAABB((Class)EntityLivingBase.class, new AxisAlignedBB(xcentered - 1.0, (double)coords.getY(), zcentered - 1.0, xcentered + 1.0, coords.getY() + 1.0 + 1.0, zcentered + 1.0));
        if (list.isEmpty()) {
            return;
        }
        Collections.shuffle(list);
        for (final EntityLivingBase entity : list) {
            if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode) {
                continue;
            }
            entity.motionX = (xcentered - entity.posX) * 0.5;
            entity.motionZ = (zcentered - entity.posZ) * 0.5;
            if (entity.motionY > -0.05) {
                entity.motionY = -0.05;
            }
            entity.attackEntityFrom((DamageSource)CropEating.damage, crop.getCurrentSize() * 2.0f);
            if (!hasMetalAromor(entity)) {
                entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 64, 50));
                entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 64, 0));
                entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 64, 0));
            }
            if (this.canGrow(crop)) {
                crop.setGrowthPoints(crop.getGrowthPoints() + 100);
            }
            crop.getWorldObj().playSound((EntityPlayer)null, xcentered, ycentered, zcentered, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1.0f, IC2.random.nextFloat() * 0.1f + 0.9f);
            crop.getCustomData().setBoolean("eaten", true);
            break;
        }
    }
    
    @Override
    public int getRootsLength(final ICropTile crop) {
        return 5;
    }
    
    @Override
    public int getGrowthDuration(final ICropTile crop) {
        float multiplier = 1.0f;
        final BlockPos coords = crop.getPosition();
        final Biome biome = BiomeUtil.getBiome(crop.getWorldObj(), coords);
        if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)) {
            multiplier /= 1.5f;
        }
        multiplier /= 1.0f + crop.getTerrainAirQuality() / 10.0f;
        return (int)(super.getGrowthDuration(crop) * multiplier);
    }
    
    private static boolean hasMetalAromor(final EntityLivingBase entity) {
        if (!(entity instanceof EntityPlayer)) {
            return false;
        }
        final EntityPlayer player = (EntityPlayer)entity;
        for (final ItemStack stack : player.inventory.armorInventory) {
            if (stack != null && ItemWrapper.isMetalArmor(stack, player)) {
                return true;
            }
        }
        return false;
    }
    
    static {
        damage = new IC2DamageSource("cropEating");
    }
}
