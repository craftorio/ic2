// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import java.io.PrintStream;
import ic2.api.energy.NodeStats;

public interface IEnergyCalculator
{
    void handleGridChange(final Grid p0);
    
    boolean runSyncStep(final EnergyNetLocal p0);
    
    boolean runSyncStep(final Grid p0);
    
    void runAsyncStep(final Grid p0);
    
    NodeStats getNodeStats(final Tile p0);
    
    void dumpNodeInfo(final Node p0, final String p1, final PrintStream p2, final PrintStream p3);
}
