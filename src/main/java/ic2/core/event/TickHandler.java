package ic2.core.event;

import ic2.core.IC2;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.level.Level;

public class TickHandler
{
	private static final boolean debugupdate = System.getProperty("ic2.debugupdate") != null;
	private static final Map<IWorldTickCallback, Throwable> debugTraces = debugupdate ? new WeakHashMap<>() : null;
	private static Throwable lastDebugTrace;

	public static void onWorldTickStart(Level world)
	{
		if (!world.isClientSide)
		{
			WorldData worldData = WorldData.get(world, false);
			if (worldData != null)
			{
				world.getProfiler().push("updates");
				processUpdates(world, worldData);
				world.getProfiler().popPush("Wind");
				worldData.windSim.updateWind();
				world.getProfiler().popPush("energynet");
				worldData.energyNet.onTickStart();
				world.getProfiler().pop();
			}
		}
	}

	public static void onWorldTickEnd(Level world)
	{
		if (!world.isClientSide)
		{
			WorldData worldData = WorldData.get(world, false);
			if (worldData != null)
			{
				world.getProfiler().push("energynet");
				worldData.energyNet.onTickEnd();
				world.getProfiler().popPush("Networking");
				IC2.network.get(true).onTickEnd(worldData);
				world.getProfiler().pop();
			}
		}
	}

	public static void onServerTick()
	{
	}

	public static void onClientTick()
	{
		IC2.keyboard.sendKeyUpdate();
		IC2.soundManager.tick();
		Level world = IC2.sideProxy.getPlayerWorld();
		if (world != null)
		{
			processUpdates(world, WorldData.get(world));
		}
	}

	public static void requestSingleWorldTick(Level world, IWorldTickCallback callback)
	{
		WorldData.get(world).singleUpdates.add(callback);
		if (debugupdate)
		{
			debugTraces.put(callback, new Throwable());
		}
	}

	public static void requestContinuousWorldTick(Level world, IWorldTickCallback update)
	{
		WorldData worldData = WorldData.get(world);
		if (!worldData.continuousUpdatesInUse)
		{
			worldData.continuousUpdates.add(update);
		} else
		{
			worldData.continuousUpdatesToRemove.remove(update);
			worldData.continuousUpdatesToAdd.add(update);
		}

		if (debugupdate)
		{
			debugTraces.put(update, new Throwable());
		}
	}

	public static void removeContinuousWorldTick(Level world, IWorldTickCallback update)
	{
		WorldData worldData = WorldData.get(world);
		if (!worldData.continuousUpdatesInUse)
		{
			worldData.continuousUpdates.remove(update);
		} else
		{
			worldData.continuousUpdatesToAdd.remove(update);
			worldData.continuousUpdatesToRemove.add(update);
		}
	}

	public static Throwable getLastDebugTrace()
	{
		return lastDebugTrace;
	}

	private static void processUpdates(Level world, WorldData worldData)
	{
		world.getProfiler().push("single-update");

		IWorldTickCallback callback;
		while ((callback = worldData.singleUpdates.poll()) != null)
		{
			if (debugupdate)
			{
				lastDebugTrace = debugTraces.remove(callback);
			}

			callback.onTick(world);
		}

		world.getProfiler().popPush("cont-update");
		worldData.continuousUpdatesInUse = true;

		for (IWorldTickCallback update : worldData.continuousUpdates)
		{
			if (debugupdate)
			{
				lastDebugTrace = debugTraces.remove(update);
			}

			update.onTick(world);
		}

		worldData.continuousUpdatesInUse = false;
		if (debugupdate)
		{
			lastDebugTrace = null;
		}

		worldData.continuousUpdates.addAll(worldData.continuousUpdatesToAdd);
		worldData.continuousUpdatesToAdd.clear();
		worldData.continuousUpdates.removeAll(worldData.continuousUpdatesToRemove);
		worldData.continuousUpdatesToRemove.clear();
		world.getProfiler().pop();
	}
}
