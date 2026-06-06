package ic2.api.crops;

public class BaseSeed
{
	public final CropCard crop;
	public final int size;
	public final int statGrowth;
	public final int statGain;
	public final int statResistance;

	public BaseSeed(CropCard crop, int size, int statGrowth, int statGain, int statResistance)
	{
		this.crop = crop;
		this.size = size;
		this.statGrowth = statGrowth;
		this.statGain = statGain;
		this.statResistance = statResistance;
	}

	@Deprecated
	public BaseSeed(CropCard crop, int size, int statGrowth, int statGain, int statResistance, int stackSize)
	{
		this(crop, size, statGrowth, statGain, statResistance);
	}
}
