package ic2.api.recipe;

import java.util.Map;

import net.minecraft.world.level.material.Fluid;

public interface ISemiFluidFuelManager extends ILiquidAcceptManager
{
	void addFluid(Fluid var1, int var2, double var3);

	ISemiFluidFuelManager.BurnProperty getBurnProperty(Fluid var1);

	Map<Fluid, ISemiFluidFuelManager.BurnProperty> getBurnProperties();

	final class BurnProperty
	{
		public final int amount;
		public final double power;

		public BurnProperty(int amount, double power)
		{
			this.amount = amount;
			this.power = power;
		}
	}
}
