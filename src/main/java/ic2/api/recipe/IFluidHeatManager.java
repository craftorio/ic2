package ic2.api.recipe;

import java.util.Map;

import net.minecraft.world.level.material.Fluid;

public interface IFluidHeatManager extends ILiquidAcceptManager
{
	void addFluid(Fluid var1, int var2, int var3);

	IFluidHeatManager.BurnProperty getBurnProperty(Fluid var1);

	Map<Fluid, IFluidHeatManager.BurnProperty> getBurnProperties();

	class BurnProperty
	{
		public final int amount;
		public final int heat;

		public BurnProperty(int amount, int heat)
		{
			this.amount = amount;
			this.heat = heat;
		}
	}
}
