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
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;

public class CropEating extends IC2CropCard {
   private final double movementMultiplier = 0.5;
   private final double length = 1.0;
   private static final IC2DamageSource damage = new IC2DamageSource("cropEating");

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
      return new String[]{"Bad", "Food"};
   }

   @Override
   public int getMaxSize() {
      return 6;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() < 3
         ? crop.getLightLevel() > 10
         : crop.isBlockBelow(Blocks.LAVA) && crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() > 10;
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return crop.getCurrentSize() >= 4 && crop.getCurrentSize() < 6 ? new ItemStack(Blocks.CACTUS) : null;
   }

   @Override
   public void tick(ICropTile crop) {
      if (crop.getCurrentSize() != 1) {
         BlockPos coords = crop.getPosition();
         double xcentered = coords.getX() + 0.5;
         double ycentered = coords.getY() + 0.5;
         double zcentered = coords.getZ() + 0.5;
         if (crop.getCustomData().getBoolean("eaten")) {
            StackUtil.dropAsEntity(crop.getWorldObj(), coords, new ItemStack(Items.ROTTEN_FLESH));
            crop.getCustomData().setBoolean("eaten", false);
         }

         List<EntityLivingBase> list = crop.getWorldObj()
            .getEntitiesWithinAABB(
               EntityLivingBase.class,
               new AxisAlignedBB(xcentered - 1.0, coords.getY(), zcentered - 1.0, xcentered + 1.0, coords.getY() + 1.0 + 1.0, zcentered + 1.0)
            );
         if (!list.isEmpty()) {
            Collections.shuffle(list);

            for (EntityLivingBase entity : list) {
               if (!(entity instanceof EntityPlayer) || !((EntityPlayer)entity).capabilities.isCreativeMode) {
                  entity.motionX = (xcentered - entity.posX) * 0.5;
                  entity.motionZ = (zcentered - entity.posZ) * 0.5;
                  if (entity.motionY > -0.05) {
                     entity.motionY = -0.05;
                  }

                  entity.attackEntityFrom(damage, crop.getCurrentSize() * 2.0F);
                  if (!hasMetalAromor(entity)) {
                     entity.addPotionEffect(new PotionEffect(MobEffects.SLOWNESS, 64, 50));
                     entity.addPotionEffect(new PotionEffect(MobEffects.INVISIBILITY, 64, 0));
                     entity.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 64, 0));
                  }

                  if (this.canGrow(crop)) {
                     crop.setGrowthPoints(crop.getGrowthPoints() + 100);
                  }

                  crop.getWorldObj()
                     .playSound(
                        null, xcentered, ycentered, zcentered, SoundEvents.ENTITY_GENERIC_EAT, SoundCategory.BLOCKS, 1.0F, IC2.random.nextFloat() * 0.1F + 0.9F
                     );
                  crop.getCustomData().setBoolean("eaten", true);
                  break;
               }
            }
         }
      }
   }

   @Override
   public int getRootsLength(ICropTile crop) {
      return 5;
   }

   @Override
   public int getGrowthDuration(ICropTile crop) {
      float multiplier = 1.0F;
      BlockPos coords = crop.getPosition();
      Biome biome = BiomeUtil.getBiome(crop.getWorldObj(), coords);
      if (BiomeDictionary.hasType(biome, BiomeDictionary.Type.SWAMP) || BiomeDictionary.hasType(biome, BiomeDictionary.Type.MOUNTAIN)) {
         multiplier /= 1.5F;
      }

      multiplier /= 1.0F + crop.getTerrainAirQuality() / 10.0F;
      return (int)(super.getGrowthDuration(crop) * multiplier);
   }

   private static boolean hasMetalAromor(EntityLivingBase entity) {
      if (!(entity instanceof EntityPlayer)) {
         return false;
      }

      EntityPlayer player = (EntityPlayer)entity;

      for (ItemStack stack : player.inventory.armorInventory) {
         if (stack != null && ItemWrapper.isMetalArmor(stack, player)) {
            return true;
         }
      }

      return false;
   }
}
