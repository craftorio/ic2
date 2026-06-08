package ic2.core.crop;

import ic2.api.crops.CropCard;
import ic2.api.crops.ICropType;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

public abstract class Ic2CropCard extends CropCard
{
	public Ic2CropCard(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public String getUnlocalizedName()
	{
		return "ic2.crop." + this.getId();
	}

	@Override
	public String getDiscoveredBy()
	{
		return "IC2 Team";
	}

	@Override
	public List<ResourceLocation> getTexturesLocation()
	{
		List<ResourceLocation> ret = new ArrayList<>(this.getMaxAge());

		for (int size = 1; size <= this.getMaxAge(); size++)
		{
			ret.add(ResourceLocation.fromNamespaceAndPath("ic2", "blocks/crop/" + this.getId() + "_" + size));
		}

		return ret;
	}
}
