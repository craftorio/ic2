package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public interface IFluidHeatManager extends ILiquidAcceptManager
{
	void addFluid(String var1, int var2, int var3);

	IFluidHeatManager.BurnProperty getBurnProperty(Fluid var1);

	Map<String, IFluidHeatManager.BurnProperty> getBurnProperties();

	class BurnProperty
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
