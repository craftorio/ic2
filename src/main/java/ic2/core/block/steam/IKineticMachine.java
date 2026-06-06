package ic2.core.block.steam;

public interface IKineticMachine
{
	int getMinimumPowerRequired();

	int getMaximumSafePower();

	void destroy();
}
