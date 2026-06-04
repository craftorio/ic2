package ic2.api.recipe;

import java.util.Map;

import net.minecraftforge.fluids.Fluid;

public interface ISemiFluidFuelManager extends ILiquidAcceptManager
{
	void addFluid(String paramString, long paramLong1, long paramLong2);

	void removeFluid(String paramString);

	FuelProperty getFuelProperty(Fluid paramFluid);

	Map<String, FuelProperty> getFuelProperties();

	public static final class FuelProperty
	{
		public final long energyPerMb;

		public final long energyPerTick;

		public FuelProperty(long energyPerMb, long energyPerTick)
		{
			this.energyPerMb = energyPerMb;
			this.energyPerTick = energyPerTick;
		}
	}
}
