package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import ic2.core.item.type.MiscResourceType;
import ic2.core.ref.ItemName;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropStickreed extends IC2CropCard {
   @Override
   public String getId() {
      return "stickreed";
   }

   @Override
   public String getDiscoveredBy() {
      return "raa1337";
   }

   @Override
   public CropProperties getProperties() {
      return new CropProperties(4, 2, 0, 1, 0, 1);
   }

   @Override
   public String[] getAttributes() {
      return new String[]{"Reed", "Resin"};
   }

   @Override
   public int getMaxSize() {
      return 4;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() < 4;
   }

   @Override
   public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air) {
      return (int)(humidity * 1.2 + nutrients + air * 0.8);
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() > 1;
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 4;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return crop.getCurrentSize() <= 3
         ? new ItemStack(Items.REEDS, crop.getCurrentSize() - 1)
         : ItemName.misc_resource.getItemStack(MiscResourceType.resin);
   }

   @Override
   public int getSizeAfterHarvest(ICropTile crop) {
      return crop.getCurrentSize() == 4 ? (byte)(3 - IC2.random.nextInt(3)) : 1;
   }

   @Override
   public boolean onEntityCollision(ICropTile crop, Entity entity) {
      return false;
   }

   @Override
   public int getGrowthDuration(ICropTile crop) {
      return crop.getCurrentSize() == 4 ? 400 : 100;
   }
}
