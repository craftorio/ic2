package ic2.core.energy.grid;

import ic2.api.energy.NodeStats;

import java.io.PrintStream;

public interface IEnergyCalculator
{
	void handleGridChange(Grid var1);

	boolean runSyncStep(EnergyNetLocal var1);

	boolean runSyncStep(Grid var1);

	void runAsyncStep(Grid var1);

	NodeStats getNodeStats(Tile var1);

	void dumpNodeInfo(Node var1, String var2, PrintStream var3, PrintStream var4);
}
