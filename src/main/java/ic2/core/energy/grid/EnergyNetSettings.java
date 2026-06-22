package ic2.core.energy.grid;

import ic2.core.init.IC2Config;

public class EnergyNetSettings
{
	public static final boolean logEnetApiAccesses = IC2Config.debug.logEnetApiAccesses.get();
	public static final boolean logEnetApiAccessTraces = IC2Config.debug.logEnetApiAccessTraces.get();
	public static final boolean logGridUpdatePerformance = false;
	public static final boolean logGridCalculationPerformance = false;
	public static final boolean roundLossDown = IC2Config.misc.roundEnetLoss.get();
	public static final int changesQueueDelay = 1;
	public static final int bfsThreshold = 2048;
	public static boolean logGridUpdateIssues = IC2Config.debug.logGridUpdateIssues.get();
	public static boolean logGridUpdatesVerbose = IC2Config.debug.logGridUpdatesVerbose.get();
	public static boolean logGridCalculationIssues = IC2Config.debug.logGridCalculationIssues.get();
}
