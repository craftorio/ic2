package ic2.core.crop;

import ic2.api.crops.CropCard;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.ResourceLocation;

public abstract class IC2CropCard extends CropCard {
  public String getOwner() {
    return "ic2";
  }
  
  public String getUnlocalizedName() {
    return "ic2.crop." + getId();
  }
  
  public String getDiscoveredBy() {
    return "IC2 Team";
  }
  
  public List<ResourceLocation> getTexturesLocation() {
    List<ResourceLocation> ret = new ArrayList<>(getMaxSize());
    for (int size = 1; size <= getMaxSize(); size++)
      ret.add(new ResourceLocation("ic2", "blocks/crop/" + getId() + "_" + size)); 
    return ret;
  }
}
