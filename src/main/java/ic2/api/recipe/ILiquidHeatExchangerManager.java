package ic2.api.recipe;

import java.util.Map;

import net.minecraft.world.level.material.Fluid;

public interface ILiquidHeatExchangerManager extends ILiquidAcceptManager
{
	void addFluid(Fluid var1, Fluid var2, int var3);

	ILiquidHeatExchangerManager.HeatExchangeProperty getHeatExchangeProperty(Fluid var1);

	Map<Fluid, ILiquidHeatExchangerManager.HeatExchangeProperty> getHeatExchangeProperties();

	ILiquidAcceptManager getSingleDirectionLiquidManager();

	record HeatExchangeProperty(Fluid outputFluid, int huPerMB)
		{
		}
}
