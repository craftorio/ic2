package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;

import java.util.List;

import net.minecraft.resources.ResourceLocation;

public abstract class CropVanillaStem extends CropVanilla
{
	public CropVanillaStem(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public List<ResourceLocation> getTexturesLocation()
	{
		return this.getDefaultTexturesLocation();
	}

	@Override
	public int getWeightInfluences(ICropTile crop, int humidity, int nutrients, int air)
	{
		return (int) (humidity * 1.1 + nutrients * 0.9 + air);
	}

	@Override
	public int getAgeAfterHarvest(ICropTile crop)
	{
		return this.getMaxAge() - 1;
	}
}
