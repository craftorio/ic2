package ic2.core.energy.grid;

import ic2.core.init.MainConfig;
import ic2.core.util.ConfigUtil;

public class EnergyNetSettings {
  public static final boolean logEnetApiAccesses = ConfigUtil.getBool(MainConfig.get(), "debug/logEnetApiAccesses");
  
  public static final boolean logEnetApiAccessTraces = ConfigUtil.getBool(MainConfig.get(), "debug/logEnetApiAccessTraces");
  
  public static boolean logGridUpdateIssues = ConfigUtil.getBool(MainConfig.get(), "debug/logGridUpdateIssues");
  
  public static boolean logGridUpdatesVerbose = ConfigUtil.getBool(MainConfig.get(), "debug/logGridUpdatesVerbose");
  
  public static boolean logGridCalculationIssues = ConfigUtil.getBool(MainConfig.get(), "debug/logGridCalculationIssues");
  
  public static final boolean logGridUpdatePerformance = false;
  
  public static final boolean logGridCalculationPerformance = false;
  
  public static final boolean roundLossDown = ConfigUtil.getBool(MainConfig.get(), "misc/roundEnetLoss");
  
  public static final int changesQueueDelay = 1;
  
  public static final double nonConductorResistance = 0.001D;
  
  public static final int bfsThreshold = 2048;
}
