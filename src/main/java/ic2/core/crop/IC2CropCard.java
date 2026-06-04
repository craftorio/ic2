// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.crop;

import java.util.ArrayList;
import net.minecraft.util.ResourceLocation;
import java.util.List;
import ic2.api.crops.CropCard;

public abstract class IC2CropCard extends CropCard
{
    @Override
    public String getOwner() {
        return "ic2";
    }
    
    @Override
    public String getUnlocalizedName() {
        return "ic2.crop." + this.getId();
    }
    
    @Override
    public String getDiscoveredBy() {
        return "IC2 Team";
    }
    
    @Override
    public List<ResourceLocation> getTexturesLocation() {
        final List<ResourceLocation> ret = new ArrayList<ResourceLocation>(this.getMaxSize());
        for (int size = 1; size <= this.getMaxSize(); ++size) {
            ret.add(new ResourceLocation("ic2", "blocks/crop/" + this.getId() + "_" + size));
        }
        return ret;
    }
}
