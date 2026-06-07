package ic2.api.crops;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

public class ExampleCropCard extends CropCard
{
	public ExampleCropCard(ICropType cropType)
	{
		super(cropType);
	}

	@Override
	public String getId()
	{
		return "example";
	}

	@Override
	public String getOwner()
	{
		return "myaddon";
	}

	@Override
	public Block getCropBlock()
	{
		return null;
	}

	@Override
	public CropProperties getProperties()
	{
		return new CropProperties(1, 0, 4, 0, 0, 2);
	}

	@Override
	public int getMaxAge()
	{
		return 5;
	}

	@Override
	public ItemStack getGain(ICropTile crop)
	{
		return new ItemStack(Items.f_42415_, 1);
	}

	@Override
	public List<ResourceLocation> getTexturesLocation()
	{
		List<ResourceLocation> ret = new ArrayList<>(this.getMaxAge());

		for (int size = 1; size <= this.getMaxAge(); size++)
		{
			ret.add(ResourceLocation.fromNamespaceAndPath("myaddon", "blocks/crop/" + this.getId() + "_" + size));
		}

		return ret;
	}
}
