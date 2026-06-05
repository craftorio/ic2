package ic2.api.crops;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class ExampleCropCard extends CropCard {
   @Override
   public String getId() {
      return "example";
   }

   @Override
   public String getOwner() {
      return "myaddon";
   }

   @Override
   public CropProperties getProperties() {
      return new CropProperties(1, 0, 4, 0, 0, 2);
   }

   @Override
   public int getMaxSize() {
      return 5;
   }

   @Override
   public ItemStack getGain(ICropTile crop) {
      return new ItemStack(Items.DIAMOND, 1);
   }

   @Override
   public List<ResourceLocation> getTexturesLocation() {
      List<ResourceLocation> ret = new ArrayList<>(this.getMaxSize());

      for (int size = 1; size <= this.getMaxSize(); size++) {
         ret.add(new ResourceLocation("myaddon", "blocks/crop/" + this.getId() + "_" + size));
      }

      return ret;
   }
}
