package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public interface IFluidHeatManager extends ILiquidAcceptManager
{
	void addFluid(String paramString, int paramInt1, int paramInt2);

	BurnProperty getBurnProperty(Fluid paramFluid);

	Map<String, BurnProperty> getBurnProperties();

	public static class BurnProperty
	{
		public final int amount;

		public final int heat;

		public BurnProperty(int amount1, int heat1)
		{
			this.amount = amount1;
			this.heat = heat1;
		}
	}
}
