package ic2.core.gui;

public abstract class FixedSizeOverlaySupplier implements IOverlaySupplier
{
	private final int width;
	private final int height;

	public FixedSizeOverlaySupplier(int size)
	{
		this(size, size);
	}

	public FixedSizeOverlaySupplier(int width, int height)
	{
		this.width = width;
		this.height = height;
	}

	@Override
	public int getUE()
	{
		return this.getUS() + this.width;
	}

	@Override
	public int getVE()
	{
		return this.getVS() + this.height;
	}
}
