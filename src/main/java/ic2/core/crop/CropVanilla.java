package ic2.core.crop;

import ic2.api.crops.ICropTile;
import net.minecraft.block.BlockCrops;
import net.minecraft.item.ItemStack;

public abstract class CropVanilla extends IC2CropCard
{
	protected final int maxAge;

	protected CropVanilla(BlockCrops block)
	{
		this(block.getMaxAge());
	}

	protected CropVanilla(int maxAge)
	{
		this.maxAge = maxAge;
	}

	@Override
	public String getDiscoveredBy()
	{
		return "Notch";
	}

	@Override
	public int getMaxSize()
	{
		return this.maxAge;
	}

	@Override
	public boolean canGrow(ICropTile crop)
	{
		return crop.getCurrentSize() < this.getMaxSize() && crop.getLightLevel() >= 9;
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
