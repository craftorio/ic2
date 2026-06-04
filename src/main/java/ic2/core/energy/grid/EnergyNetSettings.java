// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy.grid;

import ic2.core.util.ConfigUtil;
import ic2.core.init.MainConfig;

public class EnergyNetSettings
{
    public static final boolean logEnetApiAccesses;
    public static final boolean logEnetApiAccessTraces;
    public static boolean logGridUpdateIssues;
    public static boolean logGridUpdatesVerbose;
    public static boolean logGridCalculationIssues;
    public static final boolean logGridUpdatePerformance = false;
    public static final boolean logGridCalculationPerformance = false;
    public static final boolean roundLossDown;
    public static final int changesQueueDelay = 1;
    public static final double nonConductorResistance = 0.001;
    public static final int bfsThreshold = 2048;
    
    static {
        logEnetApiAccesses = ConfigUtil.getBool(MainConfig.get(), "debug/logEnetApiAccesses");
        logEnetApiAccessTraces = ConfigUtil.getBool(MainConfig.get(), "debug/logEnetApiAccessTraces");
        EnergyNetSettings.logGridUpdateIssues = ConfigUtil.getBool(MainConfig.get(), "debug/logGridUpdateIssues");
        EnergyNetSettings.logGridUpdatesVerbose = ConfigUtil.getBool(MainConfig.get(), "debug/logGridUpdatesVerbose");
        EnergyNetSettings.logGridCalculationIssues = ConfigUtil.getBool(MainConfig.get(), "debug/logGridCalculationIssues");
        roundLossDown = ConfigUtil.getBool(MainConfig.get(), "misc/roundEnetLoss");
    }
}
