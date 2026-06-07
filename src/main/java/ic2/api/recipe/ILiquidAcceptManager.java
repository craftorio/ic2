package ic2.api.recipe;

import java.util.Set;

import net.minecraft.world.level.material.Fluid;

public interface ILiquidAcceptManager
{
	boolean acceptsFluid(Fluid var1);

	Set<Fluid> getAcceptedFluids();
}
