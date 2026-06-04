// 
// Decompiled by Procyon v0.6.0
// 

package ic2.core.energy;

import ic2.core.util.Util;

public class EnergyNetGlobal
{
    public static final boolean replaceConflicting;
    public static final boolean debugTileManagement;
    public static final boolean debugGrid;
    public static final boolean debugGridVerbose;
    public static final boolean checkApi;
    public static final boolean logAll;
    
    protected static boolean verifyGrid() {
        return Util.hasAssertions();
    }
    
    static {
        replaceConflicting = (System.getProperty("ic2.energynet.replaceconflicting") != null);
        debugTileManagement = (System.getProperty("ic2.energynet.debugtilemanagement") != null);
        debugGrid = (System.getProperty("ic2.energynet.debuggrid") != null);
        debugGridVerbose = (EnergyNetGlobal.debugGrid && System.getProperty("ic2.energynet.debuggrid").equals("verbose"));
        checkApi = (System.getProperty("ic2.energynet.checkapi") != null);
        logAll = (System.getProperty("ic2.energynet.logall") != null);
    }
}
