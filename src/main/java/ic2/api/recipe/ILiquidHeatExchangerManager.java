package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public interface ILiquidHeatExchangerManager extends ILiquidAcceptManager
{
	void addFluid(String paramString1, String paramString2, int paramInt);

	HeatExchangeProperty getHeatExchangeProperty(Fluid paramFluid);

	Map<String, HeatExchangeProperty> getHeatExchangeProperties();

	ILiquidAcceptManager getSingleDirectionLiquidManager();

	public static class HeatExchangeProperty
	{
		public final Fluid outputFluid;

		public final int huPerMB;

		public HeatExchangeProperty(Fluid outputFluid, int huPerMB)
		{
			this.outputFluid = outputFluid;
			this.huPerMB = huPerMB;
		}
	}
}
