package ic2.api.recipe;

import java.util.Map;

import net.minecraft.world.level.material.Fluid;

public interface IFluidHeatManager extends ILiquidAcceptManager
{
	void addFluid(Fluid var1, int var2, int var3);

	IFluidHeatManager.BurnProperty getBurnProperty(Fluid var1);

	Map<Fluid, IFluidHeatManager.BurnProperty> getBurnProperties();

	record BurnProperty(int amount, int heat)
		{
		}
}
