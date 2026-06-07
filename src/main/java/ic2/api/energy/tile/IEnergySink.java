package ic2.api.energy.tile;

import net.minecraft.core.Direction;

public interface IEnergySink extends IEnergyAcceptor
{
	double getDemandedEnergy();

	int getSinkTier();

	double injectEnergy(Direction var1, double var2, double var4);
}
