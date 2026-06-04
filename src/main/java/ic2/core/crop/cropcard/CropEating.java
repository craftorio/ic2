package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.api.item.ItemWrapper;
import ic2.core.IC2;
import ic2.core.IC2DamageSource;
import ic2.core.crop.IC2CropCard;
import ic2.core.util.BiomeUtil;
import ic2.core.util.StackUtil;
import java.util.Collections;
import java.util.List;
import net.minecraft.block.Block;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class CropEating extends IC2CropCard {
  public String getDiscoveredBy() {
    return "Hasudako";
  }
  
  public String getId() {
    return "eatingplant";
  }
  
  public CropProperties getProperties() {
    return new CropProperties(6, 1, 1, 3, 1, 4);
  }
  
  public String[] getAttributes() {
    return new String[] { "Bad", "Food" };
  }
  
  public int getMaxSize() {
    return 6;
  }
  
  public boolean canGrow(ICropTile crop) {
    if (crop.getCurrentSize() < 3)
      return (crop.getLightLevel() > 10); 
    return (crop.isBlockBelow((Block)Blocks.LAVA) && crop.getCurrentSize() < getMaxSize() && crop.getLightLevel() > 10);
  }
  
  public int getOptimalHarvestSize(ICropTile crop) {
    return 4;
  }
  
  public boolean canBeHarvested(ICropTile crop) {
    return (crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6);
  }
  
  public ItemStack getGain(ICropTile crop) {
    if (crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6)
      return new ItemStack((Block)Blocks.CACTUS); 
    return null;
  }
  
  private final double movementMultiplier = 0.5D;
  
  private final double length = 1.0D;
  
  public void tick(ICropTile crop) {
    if (crop.getCurrentSize() == 1)
      return; 
    BlockPos coords = crop.getPosition();
    double xcentered = coords.getX() + 0.5D;
    double ycentered = coords.getY() + 0.5D;
    double zcentered = coords.getZ() + 0.5D;
    if (crop.getCustomData().getBoolean("eaten")) {
      StackUtil.dropAsEntity(crop.getWorldObj(), coords, new ItemStack(Items.ROTTEN_FLESH));
      crop.getCustomData().setBoolean("eaten", false);
    } 
    List<EntityLivingBase> list = crop.getWorldObj().getEntitiesWithinAABB(EntityLivingBase.class, new AxisAlignedBB(xcentered - 1.0D, coords
          
          .getY(), zcentered - 1.0D, xcentered + 1.0D, coords
          
          .getY() + 1.0D + 1.0D, zcentered + 1.0D));
    if (list.isEmpty())
      return; 
    Collections.shuffle(list);
    for (EntityLivingBase entity : list) {
      if (entity instanceof EntityPlayer && ((EntityPlayer)entity).capabilities.isCreativeMode)
        continue; 
      entity.motionX = (xcentered - entity.posX) * 0.5D;
      entity.motionZ = (zcentered - entity.posZ) * 0.5D;
      if (entity.motionY > -0.05D)
        entity.motionY = -0.05D; 
      entity.attackEntityFrom((DamageSource)damage, crop.getCurrentSize() * 2.0F);
      if (!hasMetalAromor(entity)) {
        entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 64, 50));
        entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 64, 0));
        entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 64, 0));
      } 
      if (canGrow(crop))
        crop.setGrowthPoints(crop.getGrowthPoints() + 100); 
      crop.getWorldObj().playSound(null, xcentered, ycentered, zcentered, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1.0F, IC2.random.nextFloat() * 0.1F + 0.9F);
      crop.getCustomData().setBoolean("eaten", true);
    } 
  }
  
  public int getRootsLength(ICropTile crop) {
    return 5;
  }
  
  public int getGrowthDuration(ICropTile crop) {
    float multiplier = 1.0F;
    BlockPos coords = crop.getPosition();
    Biome biome = BiomeUtil.getBiome(crop.getWorldObj(), coords);
    if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN))
      multiplier /= 1.5F; 
    multiplier /= 1.0F + crop.getTerrainAirQuality() / 10.0F;
    return (int)(super.getGrowthDuration(crop) * multiplier);
  }
  
  private static boolean hasMetalAromor(EntityLivingBase entity) {
    if (!(entity instanceof EntityPlayer))
      return false; 
    EntityPlayer player = (EntityPlayer)entity;
    for (ItemStack stack : player.inventory.armorInventory) {
      if (stack != null && ItemWrapper.isMetalArmor(stack, player))
        return true; 
    } 
    return false;
  }
  
  private static final IC2DamageSource damage = new IC2DamageSource("cropEating");
}
