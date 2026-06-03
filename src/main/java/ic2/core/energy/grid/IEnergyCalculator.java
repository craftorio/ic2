package ic2.core.energy.grid;

import ic2.api.energy.NodeStats;
import java.io.PrintStream;

public interface IEnergyCalculator {
  void handleGridChange(Grid paramGrid);
  
  boolean runSyncStep(EnergyNetLocal paramEnergyNetLocal);
  
  boolean runSyncStep(Grid paramGrid);
  
  void runAsyncStep(Grid paramGrid);
  
  NodeStats getNodeStats(Tile paramTile);
  
  void dumpNodeInfo(Node paramNode, String paramString, PrintStream paramPrintStream1, PrintStream paramPrintStream2);
}
