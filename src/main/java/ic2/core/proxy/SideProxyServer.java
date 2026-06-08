package ic2.core.proxy;

import ic2.api.tile.IRotorProvider;
import ic2.core.IC2;
import ic2.core.sound.SoundManager;
import ic2.core.util.Keyboard;
import ic2.core.util.Util;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.Queue;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public final class SideProxyServer implements SideProxy
{
	private static final SoundManager soundManager = new SoundManager();
	private static final Keyboard keyboard = new Keyboard();
	private static final Queue<Runnable> pendingTasks = new ArrayDeque<>();

	@Override
	public void preInit()
	{
	}

	@Override
	public void onPostInit()
	{
	}

	@Override
	public void playSoundSp(SoundEvent soundEvent, SoundSource soundCategory, float volume, float pitch)
	{
	}

	@Override
	public void playSoundOnce(Entity entity, SoundEvent soundEvent, float volume, float pitch)
	{
	}

	@Override
	public SoundManager getSoundManager()
	{
		return soundManager;
	}

	@Override
	public Keyboard getKeyboard()
	{
		return keyboard;
	}

	@Override
	public boolean isSimulating()
	{
		return true;
	}

	@Override
	public boolean isRendering()
	{
		return false;
	}

	@Override
	public void requestTick(boolean simulating, Runnable runnable)
	{
		if (!simulating)
		{
			throw new IllegalStateException();
		}

		MinecraftServer server = IC2.envProxy.getServer();
		if (server != null)
		{
			server.execute(runnable);
		} else
		{
			synchronized (pendingTasks)
			{
				server = IC2.envProxy.getServer();
				if (server != null)
				{
					server.execute(runnable);
				} else
				{
					pendingTasks.add(runnable);
				}
			}
		}
	}

	@Override
	public void onServerAvailable(MinecraftServer server)
	{
		synchronized (pendingTasks)
		{
			Runnable task;
			while ((task = pendingTasks.poll()) != null)
			{
				server.execute(task);
			}
		}
	}

	@Override
	public void displayError(String error, Object... args)
	{
		displayError0(error, args);
	}

	static void displayError0(String error, Object... args)
	{
		if (args.length > 0)
		{
			error = String.format(error, args);
		}

		error = "IndustrialCraft 2 Error\n\n == = IndustrialCraft 2 Error = == \n\n" + error + "\n\n == == == == == == == == == == ==\n";
		error = error.replace("\n", System.lineSeparator());
		throw new RuntimeException(error);
	}

	@Override
	public void displayError(Exception e, String error, Object... args)
	{
		displayError(this, e, error, args);
	}

	static void displayError(SideProxy sideProxy, Exception e, String error, Object... args)
	{
		if (args.length > 0)
		{
			error = String.format(error, args);
		}

		sideProxy.displayError("An unexpected Exception occured.\n\n" + getStackTrace(e) + "\n" + error);
	}

	private static String getStackTrace(Exception e)
	{
		StringWriter writer = new StringWriter();
		PrintWriter printWriter = new PrintWriter(writer);
		e.printStackTrace(printWriter);
		return writer.toString();
	}

	@Override
	public Player getPlayerInstance()
	{
		return null;
	}

	@Override
	public Level getWorld(MinecraftServer server, ResourceLocation dimId)
	{
		for (Level world : server.getAllLevels())
		{
			if (dimId.equals(Util.getDimId(world)))
			{
				return world;
			}
		}

		return null;
	}

	@Override
	public Level getPlayerWorld()
	{
		return null;
	}

	@Override
	public RecipeManager getRecipeManager()
	{
		return IC2.envProxy.getServer().getRecipeManager();
	}

	@Override
	public File getMinecraftDir()
	{
		return new File(".");
	}

	@Override
	public void messagePlayer(Player player, String message, Object... args)
	{
		if (player instanceof ServerPlayer)
		{
			Component msg;
			if (args.length > 0)
			{
				msg = Component.translatable(message, (Object) getMessageComponents(args));
			} else
			{
				msg = Component.translatable(message);
			}

			player.displayClientMessage(msg, false);
		}
	}

	static Component[] getMessageComponents(Object... args)
	{
		Component[] encodedArgs = new Component[args.length];

		for (int i = 0; i < args.length; i++)
		{
			if (args[i] instanceof String && ((String) args[i]).startsWith("ic2."))
			{
				encodedArgs[i] = Component.translatable((String) args[i]);
			} else
			{
				encodedArgs[i] = Component.literal(args[i].toString());
			}
		}

		return encodedArgs;
	}

	@Override
	public <T extends BlockEntity & IRotorProvider> void registerRotorProvider(BlockEntityType<T> type)
	{
	}
}
