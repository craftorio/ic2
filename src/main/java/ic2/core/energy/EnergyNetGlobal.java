package ic2.core.energy;

import ic2.core.util.Util;

public class EnergyNetGlobal {
  protected static boolean verifyGrid() {
    return Util.hasAssertions();
  }
  
  public static final boolean replaceConflicting = (System.getProperty("ic2.energynet.replaceconflicting") != null);
  
  public static final boolean debugTileManagement = (System.getProperty("ic2.energynet.debugtilemanagement") != null);
  
  public static final boolean debugGrid = (System.getProperty("ic2.energynet.debuggrid") != null);
  
  public static final boolean debugGridVerbose = (debugGrid && System.getProperty("ic2.energynet.debuggrid").equals("verbose"));
  
  public static final boolean checkApi = (System.getProperty("ic2.energynet.checkapi") != null);
  
  public static final boolean logAll = (System.getProperty("ic2.energynet.logall") != null);
}
