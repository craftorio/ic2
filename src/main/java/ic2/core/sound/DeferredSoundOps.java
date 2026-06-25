package ic2.core.sound;

import java.util.ArrayDeque;
import java.util.Queue;

/**
 * Defers mutations to the vanilla {@link net.minecraft.client.sounds.SoundEngine} until after its tick
 * completes, avoiding {@link java.util.ConcurrentModificationException} on internal HashMaps.
 */
public final class DeferredSoundOps
{
	private static final Queue<Runnable> pending = new ArrayDeque<>();

	private DeferredSoundOps()
	{
	}

	public static void run(Runnable action)
	{
		pending.add(action);
	}

	public static void flush()
	{
		Runnable action;
		while ((action = pending.poll()) != null)
		{
			action.run();
		}
	}
}