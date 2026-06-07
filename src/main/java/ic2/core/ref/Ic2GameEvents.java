package ic2.core.ref;

import ic2.core.IC2;
import net.minecraft.world.level.gameevent.GameEvent;

public class Ic2GameEvents
{
	public static final GameEvent TOOL_USE = register("tool_use");
	public static final GameEvent GENERATOR_ACTIVATE = register("generator_activate");
	public static final GameEvent GENERATOR_DEACTIVATE = register("generator_deactivate");
	public static final GameEvent MACHINE_ACTIVATE = register("machine_activate");
	public static final GameEvent MACHINE_DEACTIVATE = register("machine_deactivate");

	private static GameEvent register(String id)
	{
		return register(id, 16);
	}

	private static GameEvent register(String id, int range)
	{
		return IC2.envProxy.registerGameEvent(id, range);
	}

	public static void init()
	{
	}
}
