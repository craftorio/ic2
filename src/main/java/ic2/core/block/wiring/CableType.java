package ic2.core.block.wiring;

public enum CableType
{
	copper(1, 1, 0.25F, 0.2, 128),
	glass(0, 0, 0.25F, 0.025, 8192),
	gold(2, 1, 0.1875F, 0.4, 512),
	iron(3, 1, 0.375F, 0.8, 2048),
	tin(1, 1, 0.25F, 0.2, 32),
	detector(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192),
	splitter(0, Integer.MAX_VALUE, 0.5F, 0.5, 8192);

	public static final float insulationThickness = 0.0625F;
	public static final CableType[] values = values();
	public final int maxInsulation;
	public final int minColoredInsulation;
	public final float thickness;
	public final double loss;
	public final int capacity;

	CableType(int maxInsulation, int minColoredInsulation, float thickness, double loss, int capacity)
	{
		this.maxInsulation = maxInsulation;
		this.minColoredInsulation = minColoredInsulation;
		this.thickness = thickness;
		this.loss = loss;
		this.capacity = capacity;
	}

	public float getThickness(int insulation)
	{
		return 0.125F * insulation + this.thickness;
	}
}
