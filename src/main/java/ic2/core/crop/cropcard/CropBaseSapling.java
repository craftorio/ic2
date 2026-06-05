package ic2.core.crop.cropcard;

import ic2.api.crops.CropProperties;
import ic2.api.crops.ICropTile;
import ic2.core.IC2;
import ic2.core.crop.IC2CropCard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

public class CropBaseSapling extends IC2CropCard {
   protected final String cropName;
   protected final String saplingName;
   protected final ItemStack cropDrop;
   protected final ItemStack cropSapling;

   public CropBaseSapling(String cropName, String saplingName, ItemStack cropDrop, ItemStack cropSapling) {
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
      return new String[]{"Leaves", "Sapling", "Green"};
   }

   @Override
   public int getMaxSize() {
      return 5;
   }

   @Override
   public boolean canGrow(ICropTile crop) {
      return crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() >= 9;
   }

   @Override
   public boolean canBeHarvested(ICropTile crop) {
      return crop.getCurrentSize() == 5;
   }

   @Override
   public int getOptimalHarvestSize(ICropTile crop) {
      return 5;
   }

   @Override
   public ItemStack[] getGains(ICropTile crop) {
      List<ItemStack> drops = new ArrayList<>();
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
   public int getGrowthDuration(ICropTile crop) {
      return crop.getCurrentSize() >= 4 ? 150 : 600;
   }

   @Override
   public int getSizeAfterHarvest(ICropTile crop) {
      return 4;
   }
}
