package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.CropResItemType;
import ic2.core.ref.ItemName;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;

public class CropVenomilia extends IC2CropCard {
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
      return new String[]{"Purple", "Flower", "Tulip", "Poison"};
   }

   @Override
   public int getMaxSize() {
      return 6;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() <= 4 && crop.getLightLevel() >= 12 || crop.getCurrentSize() == 5;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() >= 4;
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      if (crop.getCurrentSize() == 5) {
         return ItemName.crop_res.getItemStack(CropResItemType.grin_powder);
      } else {
         return crop.getCurrentSize() >= 4 ? new ItemStack(Items.DYE, 1, 5) : null;
      }
   }

   @Override
   public int getSizeAfterHarvest(ICropTile crop) {
      return 3;
   }

   @Override
   public int getGrowthDuration(ICropTile crop) {
      return crop.getCurrentSize() >= 3 ? 600 : 400;
   }

   @Override
   public boolean onRightClick(ICropTile crop, EntityPlayer player) {
      if (!player.isSneaking()) {
         this.onEntityCollision(crop, player);
      }

      return crop.performManualHarvest();
   }

   @Override
   public boolean onLeftClick(ICropTile crop, EntityPlayer player) {
      if (!player.isSneaking()) {
         this.onEntityCollision(crop, player);
      }

      return crop.pick();
   }

   @Override
   public boolean onEntityCollision(ICropTile crop, Entity entity) {
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
   public boolean isWeed(ICropTile crop) {
      return crop.getCurrentSize() == 5 && crop.getStatGrowth() >= 8;
   }
}
