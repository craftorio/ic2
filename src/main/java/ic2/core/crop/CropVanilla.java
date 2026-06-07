package ic2.core.crop;

import ic2.api.crops.ICropTile;
import ic2.api.crops.ICropType;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public abstract class CropVanilla extends Ic2CropCard
{
	public CropVanilla(ICropType cropType)
	{
		super(cropType);
	}

	protected List<ResourceLocation> getDefaultTexturesLocation()
	{
		return super.getTexturesLocation();
	}

	@Override
	public List<ResourceLocation> getTexturesLocation()
	{
		List<ResourceLocation> ret = new ArrayList<>(this.getMaxAge());

		for (int size = 1; size <= this.getMaxAge(); size++)
		{
			ret.add(ResourceLocation.fromNamespaceAndPath("blocks/" + this.getId() + "_stage_" + size));
		}

		return ret;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentAge() < this.getMaxAge() && crop.getLightLevel() >= 9;
	}

	protected abstract ItemStack getSeeds();

	protected abstract ItemStack getProduct();

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return this.getProduct();
	}

	@Override
	public ItemStack getSeeds(ICropTile crop)
	{
		return crop.getStatGain() <= 1 && crop.getStatGrowth() <= 1 && crop.getStatResistance() <= 1 ? this.getSeeds() : super.getSeeds(crop);
	}
}
